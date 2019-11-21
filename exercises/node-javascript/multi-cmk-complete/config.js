// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const toml = require("@iarna/toml");
const fs = require("fs");
const untildify = require("untildify");

const config = toml.parse(fs.readFileSync("../../config.toml", "utf8"));

const { state } = toml.parse(
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

module.exports = config;
