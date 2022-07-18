// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { KmsKeyringNode, buildClient, CommitmentPolicy } from "@aws-crypto/client-node";
const { decryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)
import { S3 } from "aws-sdk";
const s3 = new S3();
import { config } from "./config";
import { EC } from "./store";
const Bucket = config.state.bucketName();

// MULTI-KMS-KEY-COMPLETE: Configure Walter
const walterKmsKey = config.state.getWalterKmsKey();
const faytheKmsKey = config.state.getFaytheKmsKey();
// MULTI-KMS-KEY-COMPLETE: Add Walter to the KMS Keys to Use
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheKmsKey, walterKmsKey] });

export type retrieveOp = {
  expectedContext?: EC;
  expectedContextKeys?: string[];
};

export function retrieve(
  Key: string,
  { expectedContext, expectedContextKeys }: retrieveOp = {}
) {
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
