// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency

import { S3 } from "aws-sdk";
const s3 = new S3();
import { config } from "./config";
import { EC } from "./store";
const Bucket = config.state.bucketName();

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.

export type retrieveOp = {
  expectedContext?: EC;
  expectedContextKeys?: string[];
};

export function retrieve(
  Key: string,
  { expectedContext, expectedContextKeys }: retrieveOp = {}
) {
  // ADD-ESDK-START: Decrypt the stream with a keyring
  return s3.getObject({ Bucket, Key }).createReadStream();
}
