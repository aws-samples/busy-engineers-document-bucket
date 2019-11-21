import io
import random
import string
from unittest import mock
from unittest.mock import call

import aws_encryption_sdk
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
    return str(PointerItem._generate_key())


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
    context_item = random_context_item()
    keys = [context_item.to_item()]
    result = {"Items": keys}
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
        Body=data, Key=pointer_item.partition_key, Metadata=pointer_item.context
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
        ctx_key = ContextItem(key, pointer_item.partition_key)
        calls.append(call(Item=ctx_key.to_item()))
    mocked_dbo.table.put_item.assert_has_calls(calls, any_order=True)


def test_query_for_context_key(mocked_dbo, random_context_key_ddb_result):
    # There is some trickery for mocking out DDB here.
    # First there's a query for a specific context key, which will return a hash-and-
    # range of one unique context key and many UUIDs (sort keys). For the conceit
    # of the test, assume two queries are made in order: one to get a context key with
    # one matching item; and one to lookup that item and its context to map to a
    # pointer. This is a bit incomplete and contrived, but simulating DDB gets
    # complex fast and this is better covered by DDBLocal (integration).

    # Pull out the context key target from the 'results'
    test_context_target = random_context_key_ddb_result["Items"][0][
        BaseItem.sort_key_name()
    ]
    # Pointer for this context key
    test_pointer = PointerItem(
        partition_key=test_context_target, context=random_context(8)
    )
    # Create a fake pointer lookup result to return for the chosen guid
    test_pointer_result = {"Items": [test_pointer.to_item()]}
    # Set up query objects
    q = ContextQuery(test_context_target)
    # Set up returning the fake DDB Item when we call query
    mock_query = mock.Mock()
    # Return first the "fake table", then the "fake pointer lookup result"
    mock_query.side_effect = [random_context_key_ddb_result, test_pointer_result]
    mocked_dbo.table.query = mock_query
    # Query
    pointers = mocked_dbo._query_for_context_key(q)
    # Assert we got back a matching Pointer record
    assert test_pointer in pointers


def test_non_unique_pointer_throws(mocked_dbo, random_context_key_ddb_result):
    # If somehow there were two entries returned on a query for a given pointer UUID
    # (i.e. something has gone wrong with enforcement of the data model), make sure
    # query throws.
    bogus_result = {
        "Items": [random_pointer_item().to_item(), random_pointer_item().to_item()]
    }
    mock_query = mock.Mock()
    mock_query.side_effect = [random_context_key_ddb_result, bogus_result]
    mocked_dbo.table.query = mock_query
    with pytest.raises(ValueError):
        mocked_dbo._query_for_context_key(ContextQuery("foobar"))


def test_list_items(mocked_dbo, random_ddb_pointer_table):
    mocked_dbo.table.scan = mock.Mock(return_value=random_ddb_pointer_table)
    expected_guids = set()
    for item in random_ddb_pointer_table["Items"]:
        partition_key = item[BaseItem.partition_key_name()]
        if not partition_key.startswith(ContextItem._prefix()):
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


def test_ec_keys_happy_case(monkeypatch, mocked_dbo):
    key = get_pointer_key()
    expected_keys = standard_context().keys()
    mocked_dbo._get_object = mock.MagicMock()
    mocked_dbo._get_pointer_item = mock.MagicMock()
    mocked_header = mock.MagicMock()
    mocked_header.encryption_context = standard_context()

    def mock_decrypt(**kwargs):
        return (None, mocked_header)

    monkeypatch.setattr(aws_encryption_sdk, "decrypt", mock_decrypt)
    mocked_dbo.retrieve(key, expected_keys)


def test_ec_keys_unhappy_case(monkeypatch, mocked_dbo):
    key = get_pointer_key()
    expected_keys = set(standard_context().keys())
    expected_keys.add("WONT_BE_PRESENT")
    mocked_dbo._get_object = mock.MagicMock()
    mocked_dbo._get_pointer_item = mock.MagicMock()
    mocked_header = mock.MagicMock()
    mocked_header.encryption_context = standard_context()

    def mock_decrypt(**kwargs):
        return (None, mocked_header)

    monkeypatch.setattr(aws_encryption_sdk, "decrypt", mock_decrypt)
    with pytest.raises(AssertionError):
        mocked_dbo.retrieve(key, expected_keys)


def test_ec_happy_case(monkeypatch, mocked_dbo):
    key = get_pointer_key()
    expected_ec = standard_context()
    mocked_dbo._get_object = mock.MagicMock()
    mocked_dbo._get_pointer_item = mock.MagicMock()
    mocked_header = mock.MagicMock()
    mocked_header.encryption_context = standard_context()

    def mock_decrypt(**kwargs):
        return (None, mocked_header)

    monkeypatch.setattr(aws_encryption_sdk, "decrypt", mock_decrypt)
    mocked_dbo.retrieve(key, expected_context=expected_ec)


def test_ec_unhappy_case(monkeypatch, mocked_dbo):
    key = get_pointer_key()
    expected_ec = standard_context()
    expected_ec["UNOBTANIUM"] = "this value will not be present!"
    mocked_dbo._get_object = mock.MagicMock()
    mocked_dbo._get_pointer_item = mock.MagicMock()
    mocked_header = mock.MagicMock()
    mocked_header.encryption_context = standard_context()

    def mock_decrypt(**kwargs):
        return (None, mocked_header)

    monkeypatch.setattr(aws_encryption_sdk, "decrypt", mock_decrypt)
    with pytest.raises(AssertionError):
        mocked_dbo.retrieve(key, expected_context=expected_ec)
