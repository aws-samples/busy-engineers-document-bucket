// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import kms = require("@aws-cdk/aws-kms");
import iam = require("@aws-cdk/aws-iam");

export class KmsKeyStack extends cdk.Stack {
  constructor(
    scope: cdk.Construct,
    id: string,
    props: cdk.StackProps,
    config: Record<string, any>
  ) {
    super(scope, id, props);

    const policyStatement = new iam.PolicyStatement();
    policyStatement.addActions(
      "kms:Create*",
      "kms:Describe*",
      "kms:Enable*",
      "kms:List*",
      "kms:Put*",
      "kms:Update*",
      "kms:Revoke*",
      "kms:Disable*",
      "kms:Get*",
      "kms:Delete*",
      "kms:ScheduleKeyDeletion",
      "kms:CancelKeyDeletion"
    );
    policyStatement.addAccountRootPrincipal();
    policyStatement.addResources("*");

    const keyPolicy = new iam.PolicyDocument();
    keyPolicy.addStatements(policyStatement);

    const kms_key = new kms.Key(this, config.kms_key_id, {
      alias: config.alias,
      policy: keyPolicy
    });

    // Output
    new cdk.CfnOutput(this, config.output, {
      value: kms_key.keyArn,
      exportName: config.export
    });
  }
}
