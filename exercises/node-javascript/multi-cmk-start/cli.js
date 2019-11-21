#!/usr/bin/env node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const program = require("commander");
const assert = require("assert");
const fs = require("fs");
const index = require("./index");

program
  .command("store [path] ")
  .description("encrypt and store the file in the s3 bucket")
  .option("-c, --context <name:value>", "Encryption Context", pairs, {})
  .action(async (path, cmdObj) => {
    assert(
      !path !== process.stdin.isTTY,
      "Source must be provided. Either stdin or a path"
    );

    const stream = path ? fs.createReadStream(path) : process.stdin;

    const file = await index.store(stream, cmdObj.context);
    console.log(file);
  });

program
  .command("retrieve <key> [path] ")
  .description("retrieve the key and decrypt from the s3 bucket")
  .option(
    "-c, --expected-context <name:value>",
    "Encryption context pairs to verify",
    pairs,
    {}
  )
  .option(
    "-k, --expected-context-keys <name>",
    "Encryption context keys to verify",
    collect,
    []
  )
  .action((key, path, cmdObj) => {
    const { expectedContext, expectedContextKeys } = cmdObj;
    const stream = path ? fs.createWriteStream(path) : process.stdout;

    index.retrieve(key, { expectedContext, expectedContextKeys }).pipe(stream);
  });

program
  .command("search <key>")
  .description("search DynamoDb dor a given encryption context key")
  .action(async key => {
    const { Items } = await index.search(key);
    console.log(Items);
  });

program
  .command("list")
  .description("list the pointers stored in DynamoDb")
  .action(async () => {
    const { Items } = await index.list();
    console.log(Items);
  });

program.parse(process.argv);

if (!process.argv.slice(2).length) {
  program.outputHelp();
}

function pairs(pair, previous) {
  const [name, value] = pair.split(":");
  assert(name && value, `Both name and value were not defined.`);
  previous[name] = value;
  return previous;
}

function collect(value, previous) {
  return previous.concat([value]);
}
