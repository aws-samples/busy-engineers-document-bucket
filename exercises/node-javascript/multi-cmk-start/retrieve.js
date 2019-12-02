// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const { decryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");
const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

// MULTI-CMK-START: Configure Walter

const faytheCMK = config.state.getFaytheCMK();
// MULTI-CMK-START: Add Walter to the CMKs to Use
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK] });

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
