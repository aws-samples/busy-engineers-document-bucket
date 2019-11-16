from __future__ import annotations

import copy
import uuid
from dataclasses import dataclass, field
from typing import Dict, Optional, Set, Union
from uuid import UUID

from boto3.dynamodb.conditions import Key  # type: ignore

from .config import config


class DataModelException(Exception):
    pass


@dataclass
class UUIDKey:
    key: Union[UUID, str]

    def __post_init__(self):
        if isinstance(self.key, str):
            # Validate that the UUID is well formed before continuing.
            self.key = str(UUID(self.key))

    def __str__(self):
        return str(self.key)


@dataclass
class BaseItem:
    partition_key: Union[UUIDKey, str]
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
        return config["document_bucket"]["document_table"]["partition_key"]

    @classmethod
    def sort_key_name(cls) -> str:
        return config["document_bucket"]["document_table"]["sort_key"]

    def _assert_set(self):
        if self.partition_key is None:
            raise DataModelException("partition_key not set correctly after init!")
        if self.sort_key is None:
            raise DataModelException("sort_key not set correctly after init!")

    def to_item(self):
        key = {
            BaseItem.partition_key_name(): self.partition_key,
            BaseItem.sort_key_name(): self.sort_key,
        }
        return key


@dataclass
class ContextQuery:
    partition_key: str

    def __post_init__(self):
        self.partition_key = ContextItem.canonicalize(self.partition_key)

    def expression(self) -> Dict[str, str]:
        return Key(BaseItem.partition_key_name()).eq(self.partition_key)


@dataclass
class PointerQuery:
    partition_key: UUIDKey

    @staticmethod
    def from_context_item(context_item) -> PointerQuery:
        return PointerQuery(context_item.sort_key)

    def expression(self) -> Dict[str, str]:
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
        context_key = context_key.upper()
        if not context_key.startswith(ContextItem._prefix()):
            context_key = ContextItem._prefix() + context_key
        return context_key

    def __post_init__(self):
        self._assert_set()
        self.partition_key = ContextItem.canonicalize(self.partition_key)
        self.sort_key = str(UUID(self.sort_key))

    @classmethod
    def from_item(cls, item: Dict[str, str]) -> ContextItem:
        partition_key = item.pop(BaseItem.partition_key_name())
        sort_key = item.pop(BaseItem.sort_key_name())
        return cls(partition_key, sort_key)


@dataclass
class PointerItem(BaseItem):
    sort_key: str = config["document_bucket"]["document_table"]["object_target"]
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
        return PointerItem(partition_key=cls._generate_key(), context=context)

    @classmethod
    def from_key_and_context(
        cls, key: Union[UUIDKey, UUID, str], context: Dict[str, str]
    ) -> PointerItem:
        if not isinstance(key, UUIDKey):
            key = UUIDKey(key)
        return cls(partition_key=key, context=context)

    @staticmethod
    def _validate_reserved_ec_keys(context: Dict[str, str]):
        pkn = BaseItem.partition_key_name()
        skn = BaseItem.sort_key_name()
        if pkn in context.keys() or skn in context.keys():
            raise DataModelException(
                "Can't use DB key names ({}, {}) as Encryption Context keys!".format(
                    pkn, skn
                )
            )

    def __post_init__(self):
        self._assert_set()
        if not isinstance(self.partition_key, UUIDKey):
            self.partition_key = UUIDKey(self.partition_key)
        PointerItem._validate_reserved_ec_keys(self.context)
        if self.sort_key != self._sort_key_config():
            raise DataModelException(
                "Sort key should be {}, was {}".format(
                    self._sort_key_config(), self.sort_key
                )
            )
        self.partition_key = str(self.partition_key)

    def context_items(self) -> Set[ContextItem]:
        result: Set[ContextItem] = set()
        for context_key in self.context.keys():
            result.add(ContextItem(context_key, self.partition_key))
        return result

    @classmethod
    def filter_for(cls):
        return Key(BaseItem.sort_key_name()).eq(cls.sort_key)

    def to_item(self):
        item = super().to_item()
        item = {**item, **copy.deepcopy(self.context)}
        return item

    @staticmethod
    def from_item(item: Dict[str, str]) -> PointerItem:
        partition_key = item.pop(BaseItem.partition_key_name())
        sort_key = item.pop(BaseItem.sort_key_name())
        return PointerItem(partition_key, sort_key, item)


@dataclass
class DocumentBundle:
    key: BaseItem
    data: bytes

    @staticmethod
    def from_data_and_context(data: bytes, context: Dict[str, str]):
        key = PointerItem.generate(context)
        return DocumentBundle(key, data)
