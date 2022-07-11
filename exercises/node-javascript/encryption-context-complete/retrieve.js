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

const faytheCMK = config.state.getFaytheCMK();
const walterCMK = config.state.getWalterCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK, walterCMK] });

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  return (
    s3
      .getObject({ Bucket, Key })
      .createReadStream()
      .pipe(decryptStream(decryptKeyring))
      // ENCRYPTION-CONTEXT-COMPLETE: Making Assertions
      .once("MessageHeader", function(header) {
        const { encryptionContext } = header;
        const pairs = Object.entries(expectedContext || {});
        const keys = (expectedContextKeys || []).slice();
        if (
          !(
            pairs.every(([key, value]) => encryptionContext[key] === value) &&
            keys.every(key =>
              Object.hasOwnProperty.call(encryptionContext, key)
            )
          )
        ) {
          this.emit(
            "error",
            new Error("Encryption context does not match expected shape")
          );
        }
      })
  );
}
