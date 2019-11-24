// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { decryptStream, KmsKeyringNode } from "@aws-crypto/client-node";
import { S3 } from "aws-sdk";
const s3 = new S3();
import { config } from "./config";
import { EC } from "./store";
const Bucket = config.state.bucketName();

const faytheCMK = config.state.getFaytheCMK();
const walterCMK = config.state.getWalterCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK, walterCMK] });

export type retrieveOp = {
  expectedContext?: EC;
  expectedContextKeys?: string[];
};

export function retrieve(
  Key: string,
  { expectedContext, expectedContextKeys }: retrieveOp = {}
) {
  // ENCRYPTION-CONTEXT-START: verify the  `expectedContext` and `expectedContextKeys` exist on the encryption context
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
