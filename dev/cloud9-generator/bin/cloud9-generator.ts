#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import "source-map-support/register";
import cdk = require("@aws-cdk/core");
import { Cloud9Stack } from "../lib/cloud9-stack";

const CLOUD9_REGION = "us-east-2";

const app = new cdk.App();

// Set up the Cloud9 IDE
new Cloud9Stack(app, "BusyEngineersCloud9Stack", {
  env: { region: CLOUD9_REGION }
});
