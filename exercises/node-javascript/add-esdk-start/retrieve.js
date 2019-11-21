// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

/* ADD-ESDK-START:
 * Add the @aws-crypto/client-node dependency.
 * You will need the decrypt stream as well as the KMS keyring.
 * Look at the config file to see how to pull the Faythe CMK.
 * Then pipe the encrypted stream to the decrypt stream: Profit.
 */

const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  return s3.getObject({ Bucket, Key }).createReadStream();
}
