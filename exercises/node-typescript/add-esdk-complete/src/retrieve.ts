// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

// ADD-ESDK-COMPLETE: Add the ESDK Dependency
import { KmsKeyringNode, buildClient, CommitmentPolicy } from "@aws-crypto/client-node";
const { decryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)

import { S3 } from "aws-sdk";
const s3 = new S3();
import { config } from "./config";
import { EC } from "./store";
const Bucket = config.state.bucketName();

// ADD-ESDK-COMPLETE: Configure the Faythe KMS Key in the Encryption SDK
const faytheKmsKey = config.state.getFaytheKmsKey();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheKmsKey] });

export type retrieveOp = {
  expectedContext?: EC;
  expectedContextKeys?: string[];
};

export function retrieve(
  Key: string,
  { expectedContext, expectedContextKeys }: retrieveOp = {}
) {
  // ADD-ESDK-COMPLETE: Add Decryption to retrieve
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
