#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { cfnExports } from "../lib/exports";
import { config } from "../lib/config";
import { createGrant, getCurrentPrinciple } from "../lib/grant_helpers";
import assert = require("assert");

!(async () => {
  const [cfnState, currentPrinciple] = await Promise.all([
    cfnExports(),
    getCurrentPrinciple()
  ]);

  const faytheKeyId = cfnState[config.faythe.export];
  const walterKeyId = cfnState[config.walter.export];

  assert(faytheKeyId && walterKeyId, "KeyIds do not exist. Deploy stacks");

  await Promise.all([
    createGrant(faytheKeyId, currentPrinciple),
    createGrant(walterKeyId, currentPrinciple)
  ]);
})();
