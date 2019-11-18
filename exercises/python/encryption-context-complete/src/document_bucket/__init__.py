import os
import sys

import toml

_CONFIG_FILE = os.path.join(sys.prefix, "config", "config.toml")

config = toml.load(_CONFIG_FILE)


def main():
    print("Entry point")
