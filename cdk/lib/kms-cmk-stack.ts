// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import kms = require("@aws-cdk/aws-kms");

export class CMKStack extends cdk.Stack {
  constructor(
    scope: cdk.Construct,
    id: string,
    props: cdk.StackProps,
    config: Record<string, any>
  ) {
    super(scope, id, props);

    const cmk = new kms.Key(this, config.cmk_id, {
      alias: config.alias
    });

    // Output
    new cdk.CfnOutput(this, config.output, {
      value: cmk.keyArn,
      exportName: config.export
    });
  }
}
