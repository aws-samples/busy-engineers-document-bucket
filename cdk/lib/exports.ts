// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import aws = require("aws-sdk");

export async function cfnExports() {
  const stuff = await Promise.all([
    cfnExportsForRegion("us-east-2"),
    cfnExportsForRegion("us-west-2")
  ])

  return Object.assign({}, ...stuff)
}

export async function cfnExportsForRegion(region: string) {
  const cf = new aws.CloudFormation({ region })
  const { Exports = [] } = await cf.listExports().promise()

  return Exports
    .filter(validExport)
    .reduce((memo, { Name, Value }) => {
      memo[Name] = Value
      return memo
    }, {} as { [key: string]: string })
}

function validExport(value: aws.CloudFormation.Export) : value is Required<aws.CloudFormation.Export> {
  return !!value.Name && !!value.Value
}
