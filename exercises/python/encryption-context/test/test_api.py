import io
import random
import string
from unittest import mock
from unittest.mock import call

import pytest
from aws_encryption_sdk import KMSMasterKeyProvider  # type: ignore
from document_bucket.api import DocumentBucketOperations
from document_bucket.model import (BaseItem, ContextItem, ContextQuery,
                                   PointerItem)


def standard_context():
    return {
        "region": "sp-moon-1",
        "fleet": "bananas",
        "user": "kilroy",
        "uptime": "42",
        "orange": "coconuts",
    }


def random_key_or_value():
    key_length = random.randint(1, 24)
    key_value = random.choices(string.printable, k=key_length)
    return "".join(key_value)


def get_pointer_key():
    return str(PointerItem._generate_uuid())


def random_context(pair_count: int):
    result = {}
    for _ in range(pair_count):
        key = random_key_or_value()
        value = random_key_or_value()
        result[key] = value
    return result


def random_context_item():
    return ContextItem(random_key_or_value(), get_pointer_key())


def random_pointer_item():
    return PointerItem.generate(random_context(random.randint(0, 10)))


@pytest.fixture
def random_ddb_pointer_table():
    num_keys = random.randint(1, 24)
    pointers = []
    table_items = []
    for _ in range(num_keys):
        p = random_pointer_item()
        pointers.append(p)
        table_items.append(p.to_item())
    result = {}
    result["Items"] = table_items
    return result


@pytest.fixture
def random_context_key_ddb_result():
    num_keys = random.randint(1, 24)
    keys = []
    result = {}
    for _ in range(num_keys):
        keys.append(random_context_item().to_key())
    result["Items"] = keys
    return result


@pytest.fixture
def pointer_item():
    return PointerItem.generate(standard_context())


@pytest.fixture
def mocked_dbo():
    mkp = mock.Mock(spec=KMSMasterKeyProvider)
    table = mock.MagicMock()
    bucket = mock.MagicMock()
    ops = DocumentBucketOperations(bucket, table, mkp)
    return ops


def test_init_happy_case():
    mkp = mock.Mock(spec=KMSMasterKeyProvider)
    table = mock.MagicMock()
    bucket = mock.MagicMock()
    ops = DocumentBucketOperations(bucket, table, mkp)
    assert ops.table is table
    assert ops.bucket is bucket
    assert ops.master_key_provider is mkp


def test_write_pointer_happy_case(mocked_dbo, pointer_item):
    mocked_dbo._write_pointer(pointer_item)
    mocked_dbo.table.put_item.assert_called_with(Item=pointer_item.to_item())


def test_write_object_happy_case(mocked_dbo, pointer_item):
    data = bytes.fromhex("cafebabe")
    mocked_dbo._write_object(data, pointer_item)
    mocked_dbo.bucket.put_object.assert_called_with(
        Body=data, Key=pointer_item.get_s3_key(), Metadata=pointer_item.context
    )


def test_get_object_happy_case(mocked_dbo, pointer_item):
    # Mock out the interaction with S3 -- set up something with expected bytes
    data = io.BytesIO(bytes.fromhex("decafbad"))

    class MockedObject(mock.MagicMock):
        def get(self):
            return {"Body": data}

    # Actually return the mock object from the "S3 call"
    mocked_dbo.bucket.Object = MockedObject
    # Now see if our method actually returned the data
    actual_bytes = mocked_dbo._get_object(pointer_item)
    assert actual_bytes == data.getvalue()


def test_populate_keys_happy_case(mocked_dbo, pointer_item):
    mocked_dbo._populate_key_records(pointer_item)
    calls = []
    for key in pointer_item.context.keys():
        ctx_key = ContextItem(key, pointer_item.get_s3_key())
        calls.append(call(Item=ctx_key.to_key()))
    mocked_dbo.table.put_item.assert_has_calls(calls, any_order=True)


def test_query_for_context_key(mocked_dbo, random_context_key_ddb_result):
    # Grab a partition key to "query" for
    test_key = random_context_key_ddb_result["Items"][0][BaseItem.partition_key_name()]
    expected_guids = set()
    for item in random_context_key_ddb_result["Items"]:
        expected_guids.add(item[BaseItem.sort_key_name()])
    # Set up the query for the key
    q = ContextQuery(test_key)
    # Set up returning the fake DDB Item when we call query
    mocked_dbo.table.query = mock.Mock(return_value=random_context_key_ddb_result)
    # Query
    pointers = mocked_dbo._query_for_context_key(q)
    # Assert the call happened
    mocked_dbo.table.query.assert_called_with(KeyConditionExpression=q.expression())
    # Assert we got a Pointer for each GUID back
    actual_guids = set()
    for pointer in pointers:
        actual_guids.add(pointer.partition_key)
    print("expected guids {}".format(expected_guids))
    print("actual guids {}".format(actual_guids))
    assert expected_guids == actual_guids


def test_list_items(mocked_dbo, random_ddb_pointer_table):
    mocked_dbo.table.scan = mock.Mock(return_value=random_ddb_pointer_table)
    expected_guids = set()
    for item in random_ddb_pointer_table["Items"]:
        partition_key = item[BaseItem.partition_key_name()]
        if not ContextItem.is_context_key_fmt(partition_key):
            expected_guids.add(partition_key)
    pointers = mocked_dbo.list()
    actual_guids = set()
    for p in pointers:
        # Ensure we filtered out context keys
        assert ContextItem._prefix() not in p.partition_key
        # Add this guid
        actual_guids.add(p.partition_key)
    assert expected_guids == actual_guids


def test_context_key(mocked_dbo):
    key = random_key_or_value()
    query = ContextQuery(key)
    mocked_dbo.search_by_context_key(key)
    mocked_dbo.table.query.assert_called_with(KeyConditionExpression=query.expression())
