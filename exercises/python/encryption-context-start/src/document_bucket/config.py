# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import os
import sys

import toml

_CONFIG_FILE = os.path.join(sys.prefix, "config", "config.toml")

config = toml.load(_CONFIG_FILE)
