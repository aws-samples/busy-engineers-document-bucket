// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

// ADD-ESDK-START: Add the dependency

const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

// ADD-ESDK-START: Plumb In Your Config

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  // ADD-ESDK-START: Decrypt the stream
  return s3.getObject({ Bucket, Key }).createReadStream();
}
