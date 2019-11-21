#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import fs = require("fs");
import toml = require("@iarna/toml");
import { config } from "../lib/config";
import { cfnExports } from "../lib/exports";
import untildify = require("untildify");

const state_file = untildify(config.base.state_file);

!(async () => {
  const cfnState = await cfnExports();

  fs.writeFileSync(state_file, toml.stringify({ state: cfnState }), {
    encoding: "utf8",
    flag: "w"
  });
})();
