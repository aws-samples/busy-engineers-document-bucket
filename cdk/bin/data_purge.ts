// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { S3, DynamoDB } from "aws-sdk";
import { config } from "../lib/config";
import { cfnExports } from "../lib/exports";

const { partition_key, sort_key } = config.document_bucket.document_table;

!(async () => {
  const s3 = new S3();
  const ddb = new DynamoDB.DocumentClient();
  const state = await cfnExports();
  const Bucket = state[config.document_bucket.bucket.export];
  const TableName = state[config.document_bucket.document_table.export];

  await Promise.all([
    purgeDynamoTable(ddb, 5, TableName),
    purgeS3Bucket(s3, Bucket)
  ]);
})();

async function purgeDynamoTable(
  ddb: DynamoDB.DocumentClient,
  chunkSize: number,
  TableName: string,
  ExclusiveStartKey?: DynamoDB.DocumentClient.Key
): Promise<void> {
  const { LastEvaluatedKey, Items = [] } = await ddb
    .scan({
      TableName,
      ExclusiveStartKey
    })
    .promise();

  while (Items.length) {
    await Promise.all(
      Items.splice(0, chunkSize).map(({ [sort_key]: s, [partition_key]: p }) =>
        ddb
          .delete({
            TableName,
            Key: { [sort_key]: s, [partition_key]: p }
          })
          .promise()
      )
    );
  }

  if (LastEvaluatedKey)
    return purgeDynamoTable(ddb, chunkSize, TableName, LastEvaluatedKey);
}

async function purgeS3Bucket(s3: S3, Bucket: string): Promise<void> {
  const { Contents = [], IsTruncated } = await s3
    .listObjects({ Bucket })
    .promise();

  if (!Contents.length) return;

  await s3
    .deleteObjects({
      Bucket,
      Delete: {
        Objects: Contents.map(({ Key }) => ({
          Key
        })) as S3.ObjectIdentifierList,
        Quiet: true
      }
    })
    .promise();

  if (IsTruncated) return purgeS3Bucket(s3, Bucket);
}
