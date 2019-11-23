// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as toml from "@iarna/toml";
import * as fs from "fs";
import untildify = require("untildify");

export const config: Record<string, any> = toml.parse(
  fs.readFileSync("../../config.toml", "utf8")
);

const { state }: Record<string, any> = toml.parse(
  fs.readFileSync(untildify(config.base.state_file), "utf8")
);

config.state = {
  bucketName() {
    return state[config.document_bucket.bucket.export];
  },

  tableName() {
    return state[config.document_bucket.document_table.export];
  },

  getFaytheCMK() {
    return state[config.faythe.export];
  },

  getWalterCMK() {
    return state[config.walter.export];
  }
};
