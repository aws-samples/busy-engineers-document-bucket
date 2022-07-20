// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const { DynamoDB } = require("aws-sdk");
const ddb = new DynamoDB.DocumentClient();
const { canonicalContextKey } = require("./store");
const config = require("./config");
const { partition_key } = config.document_bucket.document_table;
const TableName = config.state.tableName();

module.exports = search;

async function search(contextKey, { ExclusiveStartKey, Limit } = {}) {
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

