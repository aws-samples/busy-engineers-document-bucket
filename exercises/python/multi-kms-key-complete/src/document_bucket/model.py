# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

from __future__ import annotations

import copy
import uuid
from dataclasses import dataclass, field
from typing import Dict, Optional, Set, Union
from uuid import UUID

from boto3.dynamodb.conditions import Key  # type: ignore

from .config import config


class DataModelException(Exception):
    """
    Wrapper exception for errors with data model operations.
    """

    pass


@dataclass
class UUIDKey:
    """
    Models a unique identifier for document identification in the Document Bucket.
    Used for the storage of the document and for the identification of document records
    in the table.
    """

    key: Union[UUID, str]

    def __post_init__(self):
        if isinstance(self.key, str):
            # Validate that the UUID is well formed before continuing.
            self.key = str(UUID(self.key))

    def __str__(self):
        return str(self.key)


@dataclass
class BaseItem:
    """
    Models a DynamoDB record in the Document Bucket system.
    """

    #: This item's value for the partition key attribute.
    partition_key: Union[UUIDKey, str]
    #: This item's value for the sort key attribute.
    sort_key: Optional[Union[str, UUIDKey]]

    def __hash__(self):
        return hash((self.partition_key, self.sort_key))

    def __eq__(self, other):
        if self.__class__ == other.__class__:
            same_keys = other.partition_key == self.partition_key
            same_keys = same_keys and other.sort_key == self.sort_key
            return same_keys
        return False

    @classmethod
    def partition_key_name(cls) -> str:
        """
        :returns: the name of the item attribute used as the partition key.
        """
        return config["document_bucket"]["document_table"]["partition_key"]

    @classmethod
    def sort_key_name(cls) -> str:
        """
        :returns: the name of the item attribute used as the sort key.
        """
        return config["document_bucket"]["document_table"]["sort_key"]

    def _assert_set(self):
        if self.partition_key is None:
            raise DataModelException("partition_key not set correctly after init!")
        if self.sort_key is None:
            raise DataModelException("sort_key not set correctly after init!")

    def to_item(self):
        """
        Convert this item to a record format ready to write to DynamoDB.

        :returns: a dict ready to write to DynamoDB
        """
        key = {
            BaseItem.partition_key_name(): self.partition_key,
            BaseItem.sort_key_name(): self.sort_key,
        }
        return key


@dataclass
class ContextQuery:
    """
    Models queries for context keys.
    """

    partition_key: str

    def __post_init__(self):
        self.partition_key = ContextItem.canonicalize(self.partition_key)

    def expression(self) -> Dict[str, str]:
        """
        Generate a query expression for this context query.

        :returns: DynamoDB key expression ready for querying
        """
        return Key(BaseItem.partition_key_name()).eq(self.partition_key)


@dataclass
class PointerQuery:
    """
    Models queries for pointer keys.
    """

    partition_key: Union[str, UUIDKey]

    @staticmethod
    def from_key(pointer_key: str) -> PointerQuery:
        """
        :param pointer_key: the pointer key to query for
        :returns: a PointerQuery for the provided key
        """
        return PointerQuery(str(UUIDKey(pointer_key)))

    @staticmethod
    def from_context_item(context_item) -> PointerQuery:
        """
        :param context_item: the context item to use to look up the associated document
                             pointer
        :returns: a PointerQuery ready to pass to DynamoDB to look up the associated
                  pointer item
        """
        return PointerQuery(context_item.sort_key)

    def expression(self) -> Dict[str, str]:
        """
        Generate a query expression for this PointerQuery.

        :returns: DynamoDB key expression ready for querying
        """
        return Key(BaseItem.partition_key_name()).eq(self.partition_key)


@dataclass
class ContextItem(BaseItem):
    def __hash__(self):
        return super().__hash__()

    def __eq__(self, other):
        return super().__eq__(other)

    @classmethod
    def _prefix(cls) -> str:
        return config["document_bucket"]["document_table"]["ctx_prefix"].upper()

    @classmethod
    def canonicalize(cls, context_key: str) -> str:
        """
        Ensure that the provided key is in canonical form as a ContextItem partition
        key.

        :param context_key: the key to canonicalize
        :returns: the context_key updated to canonical form, if required
        """
        if not context_key.startswith(ContextItem._prefix()):
            context_key = ContextItem._prefix() + context_key
        return context_key

    def __post_init__(self):
        self._assert_set()
        self.partition_key = ContextItem.canonicalize(self.partition_key)
        self.sort_key = str(UUID(self.sort_key))

    @classmethod
    def from_item(cls, item: Dict[str, str]) -> ContextItem:
        """
        Map a raw DynamoDB item into a ContextItem.

        :param item: the item to map
        :returns: the modeled ContextItem
        """
        partition_key = item.pop(BaseItem.partition_key_name())
        sort_key = item.pop(BaseItem.sort_key_name())
        return cls(partition_key, sort_key)


@dataclass
class PointerItem(BaseItem):
    #: PointerItems have a fixed sort key
    sort_key: str = config["document_bucket"]["document_table"]["object_target"]
    #: The context for the document that this PointerItem refers to
    context: Dict[str, str] = field(default_factory=dict)

    def __hash__(self):
        # Stick to the partition and sort key as the unique identifier of the record
        return super().__hash__()

    def __eq__(self, other):
        return super().__eq__(other)

    @classmethod
    def _sort_key_config(cls) -> str:
        return config["document_bucket"]["document_table"]["object_target"]

    @classmethod
    def _generate_key(cls) -> UUIDKey:
        return UUIDKey(uuid.uuid4())

    @classmethod
    def generate(cls, context: Dict[str, str]):
        """
        Generate a new PointerItem for a new document, and its context.

        :param context: the context for the new document
        :returns: a new PointerItem for the new document
        """
        return PointerItem(partition_key=cls._generate_key(), context=context)

    @classmethod
    def from_key_and_context(
        cls, key: Union[UUIDKey, UUID, str], context: Dict[str, str]
    ) -> PointerItem:
        """
        Map the provided key and context into a modeled PointerItem.

        :param key: the key for the document that this PointerItem references
        :param context: the context for this document
        :returns: a modeled PointerItem
        """
        if not isinstance(key, UUIDKey):
            key = UUIDKey(key)
        return cls(partition_key=key, context=context)

    @staticmethod
    def _validate_reserved_ec_keys(context: Dict[str, str]):
        pkn = BaseItem.partition_key_name()
        skn = BaseItem.sort_key_name()
        if pkn in context.keys() or skn in context.keys():
            raise DataModelException(
                f"Can't use DB key names ({pkn}, {skn}) as Encryption Context keys!"
            )

    def __post_init__(self):
        self._assert_set()
        if not isinstance(self.partition_key, UUIDKey):
            self.partition_key = UUIDKey(self.partition_key)
        PointerItem._validate_reserved_ec_keys(self.context)
        if self.sort_key != self._sort_key_config():
            raise DataModelException(
                f"Sort key should be {self._sort_key_config()}, was {self.sort_key}"
            )
        self.partition_key = str(self.partition_key)

    def context_items(self) -> Set[ContextItem]:
        """
        Get the set of ContextItems for this PointerItem record.

        :returns: a ContextItem for each key of context for this PointerItem
        """
        result: Set[ContextItem] = set()
        for context_key in self.context.keys():
            result.add(ContextItem(context_key, self.partition_key))
        return result

    @classmethod
    def filter_for(cls):
        """
        Helper to provide a DynamoDB filter to select PointerItem records

        :returns: a key expression filter ready to use with DynamoDB operations
        """
        return Key(BaseItem.sort_key_name()).eq(cls.sort_key)

    def to_item(self):
        item = super().to_item()
        item = {**item, **copy.deepcopy(self.context)}
        return item

    @staticmethod
    def from_item(item: Dict[str, str]) -> PointerItem:
        """
        Map a raw DynamoDB item into a PointerItem.

        :param item: the item to map
        :returns: the modeled PointerItem
        """
        partition_key = item.pop(BaseItem.partition_key_name())
        sort_key = item.pop(BaseItem.sort_key_name())
        return PointerItem(partition_key, sort_key, item)


@dataclass
class DocumentBundle:
    """
    The complete, aggregated form of a Document Bucket document. Contains the data,
    the unique identifier key, and the context.
    """

    key: BaseItem
    data: bytes

    @staticmethod
    def from_pointer_and_data(key: PointerItem, data: bytes):
        """
        Build a bundled document from the provdied document key and data.

        :returns: a DocumentBundle for this pointer and data
        """
        return DocumentBundle(key, data)

    @staticmethod
    def from_data_and_context(data: bytes, context: Dict[str, str]):
        """
        Build a bundled document from the provided data and document context.

        :returns: a DocumentBundle for this data and context
        """
        key = PointerItem.generate(context)
        return DocumentBundle(key, data)
