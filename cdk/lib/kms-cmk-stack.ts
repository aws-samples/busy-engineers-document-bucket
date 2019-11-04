// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import kms = require("@aws-cdk/aws-kms");

interface CMKStackProps extends cdk.StackProps {
  alias: string;
}

export class CMKStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props: CMKStackProps) {
    super(scope, id, props);

    new kms.Key(this, props.alias, {
      alias: props.alias
    });

    // TODO: Grants
  }
}
