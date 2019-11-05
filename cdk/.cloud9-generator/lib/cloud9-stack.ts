// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import c9 = require("@aws-cdk/aws-cloud9");

/**
 * This stack is meant to be synth'd to a CFN template and launched from the console.
 * That provides the kickoff point for running the workshop in any account.
 */
export class Cloud9Stack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const INSTANCE_TYPE = "t3.medium";

    // Build the Cloud9 instance
    const c9instance = new c9.CfnEnvironmentEC2(
      this,
      "BusyEngineersCloud9IDE",
      {
        instanceType: INSTANCE_TYPE,
        description: "Busy Engineer's Document Bucket Cloud9 IDE",
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
