// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

// ADD-ESDK-COMPLETE: Add the ESDK Dependency
const { KmsKeyringNode, buildClient, CommitmentPolicy } = require("@aws-crypto/client-node");
const { encryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)

const assert = require("assert");
const { S3, DynamoDB } = require("aws-sdk");
const uuid = require("uuid");
const s3 = new S3();
const ddb = new DynamoDB.DocumentClient();

const config = require("./config");
const {
  partition_key,
  sort_key,
  ctx_prefix,
  object_target
} = config.document_bucket.document_table;
const TableName = config.state.tableName();
const Bucket = config.state.bucketName();

// ADD-ESDK-COMPLETE: Configure the Faythe KMS Key in the Encryption SDK
const faytheKmsKey = config.state.getFaytheKmsKey();
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheKmsKey
});

const contextPrefix = ctx_prefix.toUpperCase();

module.exports = store;
module.exports.canonicalContextKey = canonicalContextKey;

async function store(fileStream, encryptionContext = {}) {
  /* The encryption context can not step on the Dynamo DB indexing structure. */
  assert(
    !encryptionContext[partition_key] && !encryptionContext[sort_key],
    `Can't use DB key names ${partition_key} or ${sort_key} as Encryption Context keys!`
  );

  const Key = uuid.v4();

  const PointerItem = ddbItem(Key, object_target, encryptionContext);
  const ContextItems = Object.keys(encryptionContext)
    .map(canonicalContextKey)
    .map(canonicalKey => ddbItem(canonicalKey, Key));

  // ADD-ESDK-COMPLETE: Add Encryption to store
  const Body = fileStream.pipe(encryptStream(encryptKeyring));

  const file = await s3
    .upload({ Bucket, Key, Body, Metadata: encryptionContext })
    .promise();

  for (Item of [PointerItem, ...ContextItems]) {
    await ddb.put({ Item, TableName }).promise();
  }

  return file;
}

function canonicalContextKey(contextKey) {
  return `${contextPrefix}${contextKey}`;
}

function ddbItem(partitionValue, sortValue, op = {}) {
  return { ...op, [partition_key]: partitionValue, [sort_key]: sortValue };
}
