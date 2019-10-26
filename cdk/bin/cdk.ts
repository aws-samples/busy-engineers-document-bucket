#!/usr/bin/env node
import 'source-map-support/register';
import cdk = require('@aws-cdk/core');
import { CMKStack } from '../lib/kms-cmk-stack';
import { DocumentBucketStack } from '../lib/document-bucket-stack';

const REGION_A = "us-east-2";
const REGION_B = "us-west-2";
const WEBSITE_REGION = "us-east-2";

const app = new cdk.App();

// Initialize CMK Stacks
new CMKStack(app, "FaytheCMKStack", {
    env: {region: REGION_A},
    alias: "FaytheCMK" });

new CMKStack(app, "WalterCMKStack", {
    env: {region: REGION_B},
    alias: "WalterCMK" });

// Initialize Document Bucket resources
new DocumentBucketStack(app, "DocumentBucketStack", {
    env: {region: WEBSITE_REGION}});
