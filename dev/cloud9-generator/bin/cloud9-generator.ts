#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import "source-map-support/register";
import cdk = require("@aws-cdk/core");
import { Cloud9Stack } from "../lib/cloud9-stack";
import toml = require("@iarna/toml");
import fs = require("fs");

const config: any = toml.parse(fs.readFileSync("../../config.toml", "utf8"));
const CLOUD9_REGION = config.cloud9.region;
const LAUNCH_URL = config.cloud9.launch_url;

const app = new cdk.App();

// Set up the Cloud9 IDE
new Cloud9Stack(app, "BusyEngineersCloud9Stack", {
  env: { region: CLOUD9_REGION }
});

console.error("Cloud9 convenience launch link: " + LAUNCH_URL);
