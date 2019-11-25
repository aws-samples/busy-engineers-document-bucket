// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {
  decryptStream,
  KmsKeyringNode,
  MessageHeader
} from "@aws-crypto/client-node";
import { S3 } from "aws-sdk";
const s3 = new S3();
import { config } from "./config";
import { EC } from "./store";
import { Writable } from "stream";
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
  // ENCRYPTION-CONTEXT-COMPLETE: Making Assertions
  const verify = verifyFn(expectedContext, expectedContextKeys);
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring))
    .once("MessageHeader", function(this: Writable, header) {
      if (!verify(header)) {
        this.emit(
          "error",
          new Error("Encryption context does not match expected shape")
        );
      }
    });
}

export function verifyFn(
  expectedContext: EC = {},
  expectedContextKeys: string[] = []
) {
  const pairs = Object.entries(expectedContext);
  const keys = expectedContextKeys.slice();

  return function verify({ encryptionContext }: MessageHeader) {
    return (
      pairs.every(([key, value]) => encryptionContext[key] === value) &&
      keys.every(key => Object.hasOwnProperty.call(encryptionContext, key))
    );
  };
}
