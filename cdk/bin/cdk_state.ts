#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import aws = require("aws-sdk");
import fs = require("fs");
import toml = require("@iarna/toml");
import { config } from "../lib/config"
import untildify = require('untildify')

const cf = new aws.CloudFormation()
const state_file = untildify(config.base.state_file)

!(async () => {
  const { Exports = [] } = await cf.listExports().promise()

  const cfnState  = Exports
    .filter(validExport)
    .reduce((memo, { Name, Value }) => {
      memo[Name] = Value
      return memo
    }, {} as { [key: string]: string })
  
  fs.writeFileSync(state_file, toml.stringify({ state: cfnState }), {encoding: 'utf8', flag: 'w'})
})()

function validExport(value: aws.CloudFormation.Export) : value is Required<aws.CloudFormation.Export> {
  return !!value.Name && !!value.Value
}
