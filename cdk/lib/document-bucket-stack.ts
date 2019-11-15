// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import s3 = require("@aws-cdk/aws-s3");
import ddb = require("@aws-cdk/aws-dynamodb");

export class DocumentBucketStack extends cdk.Stack {
  constructor(
    scope: cdk.Construct,
    id: string,
    props: cdk.StackProps,
    config: Record<string, any>
  ) {
    super(scope, id, props);

    const s3Bucket = new s3.Bucket(this, config.bucket.name, {
      accessControl: s3.BucketAccessControl.PRIVATE,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      removalPolicy: cdk.RemovalPolicy.DESTROY
    });

    const ddbTable = new ddb.Table(this, config.document_table.name, {
      partitionKey: {
        name: config.document_table.partition_key,
        type: ddb.AttributeType.STRING
      },
      sortKey: {
        name: config.document_table.sort_key,
        type: ddb.AttributeType.STRING
      },
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      billingMode: ddb.BillingMode.PAY_PER_REQUEST
    });

    // Outputs
    new cdk.CfnOutput(this, config.document_table.output, {
      value: ddbTable.tableName,
      exportName: config.document_table.export
    });

    new cdk.CfnOutput(this, config.bucket.output, {
      value: s3Bucket.bucketName,
      exportName: config.bucket.export
    });
  }
}
