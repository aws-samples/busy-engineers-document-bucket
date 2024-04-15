// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require('aws-cdk-lib');
import * as c9 from 'aws-cdk-lib/aws-cloud9';

/**
 * This stack is meant to be synth'd to a CFN template and launched from the console.
 * That provides the kickoff point for running the workshop in any account.
 */
export class Cloud9Stack extends cdk.Stack {
  constructor(scope: cdk.App, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const instanceType = "t3.medium";
    const imageId = 'resolve:ssm:/aws/service/cloud9/amis/amazonlinux-2023-x86_64';
    const description = "Busy Engineer's Document Bucket Cloud9 IDE"

    // Build the Cloud9 instance
    const c9instance = new c9.CfnEnvironmentEC2(
      this,
      "BusyEngineersCloud9IDE",
      {
        instanceType,
        imageId,
        description,
        name: "BusyEngineersCloud9IDE",
        repositories: [
          {
            pathComponent: "workshop",
            repositoryUrl:
              "https://github.com/aws-samples/busy-engineers-document-bucket.git"
          }
        ]
      }
    );
  }
}
