#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import "source-map-support/register";
import cdk = require("@aws-cdk/core");
import { CMKStack } from "../lib/kms-cmk-stack";
import { DocumentBucketStack } from "../lib/document-bucket-stack";
import { WebsiteStack } from "../lib/website-stack";
import fs = require("fs");
import toml = require("@iarna/toml");

// Load our configuration file to bootstrap CFN configuration.
const config: Record<string, any> = toml.parse(
  fs.readFileSync("../config.toml", "utf8")
);

// Map constants
const STATE_FILE = config.base.state_file;
const FAYTHE_CMK_REGION = config.faythe_cmk.region;
const FAYTHE_CMK_ALIAS = config.faythe_cmk.alias;
const WALTER_CMK_REGION = config.walter_cmk.region;
const WALTER_CMK_ALIAS = config.walter_cmk.alias;

const WEBSITE_CONFIG = config.website;

const app = new cdk.App();

// Initialize CMK Stacks
new CMKStack(app, "BusyEngineersFaytheCMKStack", {
  env: { region: FAYTHE_CMK_REGION },
  alias: FAYTHE_CMK_ALIAS
});

new CMKStack(app, "BusyEngineersWalterCMKStack", {
  env: { region: WALTER_CMK_REGION },
  alias: WALTER_CMK_ALIAS
});

// Initialize Document Bucket resources
new DocumentBucketStack(app, "BusyEngineersDocumentBucketStack", {
  env: { region: WEBSITE_CONFIG.region }
});

// Initialize client website resources
const websiteStack = new WebsiteStack(
  app,
  "BusyEngineersWebsiteStack",
  {
    env: { region: WEBSITE_CONFIG.region }
  },
  WEBSITE_CONFIG
);
