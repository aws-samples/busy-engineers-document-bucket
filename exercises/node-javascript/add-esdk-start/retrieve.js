// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency

const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  // ADD-ESDK-START: Decrypt the stream with a keyring
  return s3.getObject({ Bucket, Key }).createReadStream();
}
