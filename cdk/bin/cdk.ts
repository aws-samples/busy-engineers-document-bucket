#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import "source-map-support/register";
import cdk = require("@aws-cdk/core");
import { CMKStack } from "../lib/kms-cmk-stack";
import { DocumentBucketStack } from "../lib/document-bucket-stack";
import { WebappStack } from "../lib/webapp-stack";
import fs = require("fs");
import toml = require("@iarna/toml");

// Load our configuration file to bootstrap CFN configuration.
const config: Record<string, any> = toml.parse(
  fs.readFileSync("../config.toml", "utf8")
);

// Map constants
const STATE_FILE = config.base.state_file;
const WEBAPP_CONFIG = config.webapp;
const BUCKET_CONFIG = config.document_bucket;
const FAYTHE_CONFIG = config.faythe;
const WALTER_CONFIG = config.walter;

const app = new cdk.App();

// Initialize CMK Stacks
new CMKStack(
  app,
  FAYTHE_CONFIG.stack_id,
  {
    env: { region: FAYTHE_CONFIG.region }
  },
  FAYTHE_CONFIG
);

new CMKStack(
  app,
  WALTER_CONFIG.stack_id,
  {
    env: { region: WALTER_CONFIG.region }
  },
  WALTER_CONFIG
);

// Initialize Document Bucket resources
new DocumentBucketStack(
  app,
  BUCKET_CONFIG.stack_id,
  {
    env: { region: BUCKET_CONFIG.region }
  },
  BUCKET_CONFIG
);

// Initialize client webapp resources
const webappStack = new WebappStack(
  app,
  WEBAPP_CONFIG.stack_id,
  {
    env: { region: WEBAPP_CONFIG.region }
  },
  WEBAPP_CONFIG
);
