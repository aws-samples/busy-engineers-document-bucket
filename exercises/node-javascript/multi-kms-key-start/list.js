// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const { DynamoDB } = require("aws-sdk");
const ddb = new DynamoDB.DocumentClient();
const config = require("./config");
const { sort_key, object_target } = config.document_bucket.document_table;
const TableName = config.state.tableName();

module.exports = list;

async function list({ ExclusiveStartKey, Limit } = {}) {
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

