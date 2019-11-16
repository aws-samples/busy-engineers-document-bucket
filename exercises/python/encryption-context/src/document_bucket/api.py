from typing import Dict, Set

import aws_encryption_sdk  # type: ignore
from aws_encryption_sdk import KMSMasterKeyProvider  # type: ignore

from .model import (BaseItem, ContextItem, ContextQuery, DocumentBundle,
                    PointerItem)


class DocumentBucketOperations:
    def __init__(self, bucket, table, master_key_provider: KMSMasterKeyProvider):
        self.bucket = bucket
        self.table = table
        self.master_key_provider: KMSMasterKeyProvider = master_key_provider

    def _write_pointer(self, item: PointerItem):
        self.table.put_item(Item=item.to_item())

    def _write_object(self, data: bytes, item: PointerItem):
        s3object = self.bucket.put_object(
            Body=data, Key=item.get_s3_key(), Metadata=item.context
        )
        return s3object

    def _get_object(self, item: PointerItem) -> bytes:
        s3object = self.bucket.Object(item.get_s3_key()).get()
        return s3object["Body"].read()

    def _populate_key_records(self, pointer: PointerItem) -> Set[ContextItem]:
        context_items: Set[ContextItem] = pointer.context_items()
        for context_item in context_items:
            self.table.put_item(Item=context_item.to_key())
        return context_items

    # JSONDecoder
    def _query_for_context_key(self, query: ContextQuery) -> Set[PointerItem]:
        result = self.table.query(KeyConditionExpression=query.expression())
        pointers: Set[PointerItem] = set()
        for ddb_item in result["Items"]:
            key = ddb_item[BaseItem.sort_key_name()]
            pointer = PointerItem(key)
            pointer_data = self.table.get_item(pointer).get("Item")
            pointer.context = pointer.context_from_item(pointer_data)
            pointers.add(pointer)
        return pointers

    def _scan_table(self) -> Set[PointerItem]:
        result = self.table.scan(PointerItem.filter_for())
        pointers = set()
        for ddb_item in result["Items"]:
            partition_key = ddb_item[BaseItem.partition_key_name()]
            # TODO Use the JSONDecoder
            pointer = PointerItem(partition_key)
            pointers.add(pointer)
        return pointers

    def list(self) -> Set[PointerItem]:
        return self._scan_table()

    def retrieve(
        self,
        pointer_key: str,
        expected_context_keys: Set[str] = set(),
        expected_context: Dict[str, str] = {},
    ) -> DocumentBundle:
        item = PointerItem.from_key_and_context(pointer_key, expected_context)
        encrypted_data = self._get_object(item)
        (plaintext, header) = aws_encryption_sdk.decrypt(
            source=encrypted_data, key_provider=self.master_key_provider
        )
        if expected_context_keys not in header.encryption_context.keys():
            raise AssertionError(
                "Encryption context assertion failed! Expected: {} but got {}".format(
                    expected_context_keys, header.encryption_context
                )
            )
        if expected_context.items() not in header.encryption_context.items():
            raise AssertionError(
                "Encryption context assertion failed! Expected: {} but got {}".format(
                    expected_context, header.encryption_context
                )
            )
        return DocumentBundle.from_data_and_context(
            plaintext, header.encryption_context
        )

    def store(self, data: bytes, context: Dict[str, str]) -> PointerItem:
        encrypted_data = aws_encryption_sdk.encrypt(
            source=data,
            key_provider=self.master_key_provider,
            encryption_context=context,
        )
        item = PointerItem.generate(context)
        self._write_pointer(item)
        self._write_object(encrypted_data, item)
        self._populate_key_records(item)
        return item

    def search_by_context_key(self, context_key: str) -> Set[PointerItem]:
        key = ContextQuery(context_key)
        return self._query_for_context_key(key)
