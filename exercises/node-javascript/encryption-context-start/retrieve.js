// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const { decryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");
const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

const faytheCMK = config.state.getFaytheCMK();
const walterCMK = config.state.getWalterCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK, walterCMK] });

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  /* ENCRYPTION-CONTEXT-START:
   * The included `expectedContext` and `expectedContextKeys` must be validated.
   * The AWS Encryption SDK decrypt stream will emit a `MessageHeader` event.
   * This event will pass the parsed header.
   * The header will have a property `encryptionContext`,
   * that contains the validated encryption context for this message.
   */
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
