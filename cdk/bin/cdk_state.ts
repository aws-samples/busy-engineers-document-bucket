#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import aws = require("aws-sdk");
import fs = require("fs");
import toml = require("@iarna/toml");
import { config } from "../lib/config"

const cf = new aws.CloudFormation()

!(async () => {
  const { Exports = [] } = await cf.listExports().promise()

  const cfnState  = Exports
    .filter(validExport)
    .reduce((memo, { Name, Value }) => {
      memo[Name] = Value
      return memo
    }, {} as { [key: string]: string })
  
  fs.writeFileSync(config.base.state_file, toml.stringify({ state: cfnState }), {encoding: 'utf8'})
})()

function validExport(value: aws.CloudFormation.Export) : value is Required<aws.CloudFormation.Export> {
  return !!value.Name && !!value.Value
}
