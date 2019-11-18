import fs = require("fs");
import toml = require("@iarna/toml");

// Load our configuration file to bootstrap CFN configuration.
export const config: Record<string, any> = toml.parse(
  fs.readFileSync("../exercises/config.toml", "utf8")
);
