#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { cfnExports } from "../lib/exports"
import { config } from "../lib/config"
import { revokeGrant, getCurrentPrinciple } from "../lib/grant_helpers"

!(async () => {
  const [cfnState, currentPrinciple]  = await Promise.all([
    cfnExports(),
    getCurrentPrinciple()
  ])

  const faytheKeyId = cfnState[config.faythe.export]
  const walterKeyId = cfnState[config.walter.export]
  
  await Promise.all([
    revokeGrant(faytheKeyId, currentPrinciple),
    revokeGrant(walterKeyId, currentPrinciple),
  ])

})()
