// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { DynamoDB } from "aws-sdk";
const ddb = new DynamoDB.DocumentClient();
import { config } from "./config";
const { canonicalContextKey } = require("./store");
const { partition_key } = config.document_bucket.document_table;
const TableName = config.state.tableName();

export type searchOp = {
  ExclusiveStartKey?: DynamoDB.DocumentClient.Key;
  Limit?: number;
};
export async function search(
  contextKey: string,
  { ExclusiveStartKey, Limit }: searchOp = {}
) {
  return ddb
    .query({
      TableName,
      KeyConditionExpression: "#HashKey = :hkey",
      ExpressionAttributeValues: {
        ":hkey": canonicalContextKey(contextKey)
      },
      ExpressionAttributeNames: {
        "#HashKey": partition_key
      },
      ExclusiveStartKey,
      Limit
    })
    .promise();
}
