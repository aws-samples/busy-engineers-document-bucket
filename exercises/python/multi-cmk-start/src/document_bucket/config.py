# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import os
import sys

import toml

"""
Loads and maps configuration from the Document Bucket configuration system.
"""

_CONFIG_FILE = os.path.join(sys.prefix, "config", "config.toml")

config = toml.load(_CONFIG_FILE)
