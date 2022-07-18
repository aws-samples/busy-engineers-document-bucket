// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { DynamoDB } from "aws-sdk";
const ddb = new DynamoDB.DocumentClient();
import { config } from "./config";

const { sort_key, object_target } = config.document_bucket.document_table;
const TableName = config.state.tableName();

export type ListOp = {
  ExclusiveStartKey?: DynamoDB.DocumentClient.Key;
  Limit?: number;
};
export async function list({ ExclusiveStartKey, Limit }: ListOp = {}) {
  return ddb
    .scan({
      TableName,
      FilterExpression: "#RangeKey = :rkey",
      ExpressionAttributeValues: {
        ":rkey": object_target
      },
      ExpressionAttributeNames: {
        "#RangeKey": sort_key
      },
      ExclusiveStartKey,
      Limit
    })
    .promise();
}
