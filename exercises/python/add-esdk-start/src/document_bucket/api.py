# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

from typing import Dict, Set

from .model import ContextItem, ContextQuery, DocumentBundle, PointerItem, PointerQuery

# ADD-ESDK-START: Add the ESDK Dependency


class DocumentBucketOperations:
    """
    Operations available for interaction with the Document Bucket.
    """

    # ADD-ESDK-START: Add the ESDK Dependency
    def __init__(self, bucket, table):
        """
        Initialize a new operations object with the provided arguments.

        Args:
            bucket: S3 bucket for storing document objects
            table: DynamoDB table for storing document pointers and context keys
        """
        self.bucket = bucket
        self.table = table
        # ADD-ESDK-START: Add the ESDK Dependency

    def _write_pointer(self, item: PointerItem):
        self.table.put_item(Item=item.to_item())

    def _write_object(self, data: bytes, item: PointerItem):
        s3object = self.bucket.put_object(
            Body=data, Key=item.partition_key, Metadata=item.context
        )
        return s3object

    def _get_object(self, item: PointerItem) -> bytes:
        s3object = self.bucket.Object(item.partition_key).get()
        return s3object["Body"].read()

    def _populate_key_records(self, pointer: PointerItem) -> Set[ContextItem]:
        context_items: Set[ContextItem] = pointer.context_items()
        for context_item in context_items:
            self.table.put_item(Item=context_item.to_item())
        return context_items

    def _get_pointer_item(self, pointer_query: PointerQuery) -> PointerItem:
        pointer_items = self.table.query(
            KeyConditionExpression=pointer_query.expression()
        )["Items"]
        if len(pointer_items) != 1:
            raise ValueError(
                f"Pointer ID not unique! Expected 1 ID, got {len(pointer_items)}"
            )
        return PointerItem.from_item(pointer_items[0])

    def _query_for_context_key(self, query: ContextQuery) -> Set[PointerItem]:
        result = self.table.query(KeyConditionExpression=query.expression())
        pointers: Set[PointerItem] = set()
        for ddb_context_item in result["Items"]:
            context_item = ContextItem.from_item(ddb_context_item)
            pointer_query = PointerQuery.from_context_item(context_item)
            pointers.add(self._get_pointer_item(pointer_query))
        return pointers

    def _scan_table(self) -> Set[PointerItem]:
        result = self.table.scan(FilterExpression=PointerItem.filter_for())
        pointers = set()
        for ddb_item in result["Items"]:
            pointer = PointerItem.from_item(ddb_item)
            pointers.add(pointer)
        return pointers

    def list(self) -> Set[PointerItem]:
        """
        List all of the inventoried items in the Document Bucket system.

        :returns: the set of pointers to Document Bucket documents
        """
        return self._scan_table()

    def retrieve(
        self,
        pointer_key: str,
        expected_context_keys: Set[str] = set(),
        expected_context: Dict[str, str] = {},
    ) -> DocumentBundle:
        """
        Retrieves a document from the Document Bucket system.

        :param pointer_key: the key for the document to retrieve
        :param expected_context_keys: TODO do something with this parameter :)
        :param expected_context: TODO do something with this parameter :)
        :returns: the document, its key, and associated context
        """
        # ADD-ESDK-START: Add Decryption to retrieve
        item = self._get_pointer_item(PointerQuery.from_key(pointer_key))
        data = self._get_object(item)
        return DocumentBundle.from_data_and_context(data, item.context)

    def store(self, data: bytes, context: Dict[str, str] = {}) -> PointerItem:
        """
        Stores a document in the Document Bucket system.

        :param data: the bytes of the document to store
        :param context: TODO do something with this parameter :)
        :returns: the pointer reference for this document in the Document Bucket system
        """
        # ADD-ESDK-START: Add Encryption to store
        item = PointerItem.generate(context)
        self._write_pointer(item)
        self._write_object(data, item)
        self._populate_key_records(item)
        return item

    def search_by_context_key(self, context_key: str) -> Set[PointerItem]:
        """
        Search for the documents in the Document Bucket system that have the provided
        key in their context.

        :param context_key: the context key for which to find matching documents
        :returns: the pointers for the matching documents, if any
        """
        key = ContextQuery(context_key)
        return self._query_for_context_key(key)
