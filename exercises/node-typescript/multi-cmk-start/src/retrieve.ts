// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { decryptStream, KmsKeyringNode } from "@aws-crypto/client-node";
import { S3 } from "aws-sdk";
const s3 = new S3();
import { config } from "./config";
import { EC } from "./store";
const Bucket = config.state.bucketName();

// MULTI-CMK-START: Add the WalterCMK

const faytheCMK = config.state.getFaytheCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK] });

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
