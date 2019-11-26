#!/usr/bin/env ts-node

// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as program from "commander";
import * as assert from "assert";
import * as fs from "fs";
import { list, search, store, retrieve } from "./src/index";

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

    const file = await store(stream, cmdObj.context);
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

    retrieve(key, { expectedContext, expectedContextKeys }).pipe(stream);
  });

program
  .command("search <key>")
  .description("search DynamoDb dor a given encryption context key")
  .action(async key => {
    const { Items } = await search(key);
    console.log(Items);
  });

program
  .command("list")
  .description("list the pointers stored in DynamoDb")
  .action(async () => {
    const { Items } = await list();
    console.log(Items);
  });

program.parse(process.argv);

if (!process.argv.slice(2).length) {
  program.outputHelp();
}

function pairs(pair: string, previous: any) {
  const [name, value] = pair.split(":");
  assert(name && value, `Both name and value were not defined.`);
  previous[name] = value;
  return previous;
}

function collect(value: string, previous: any) {
  return previous.concat([value]);
}
