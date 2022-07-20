// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const { KmsKeyringNode, buildClient, CommitmentPolicy } = require("@aws-crypto/client-node");
const { decryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)
const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

// MULTI-KMS-KEY-COMPLETE: Configure Walter
const walterKmsKey = config.state.getWalterKmsKey();
const faytheKmsKey = config.state.getFaytheKmsKey();
// MULTI-KMS-KEY-COMPLETE: Add Walter to the KMS Keys to Use
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheKmsKey, walterKmsKey] });

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
