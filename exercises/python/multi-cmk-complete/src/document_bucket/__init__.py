# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import os

import aws_encryption_sdk  # type: ignore
import boto3  # type: ignore
import toml

from .api import DocumentBucketOperations
from .config import config


def initialize() -> DocumentBucketOperations:
    # Load the pointers to CloudFormation resources that you just deployed
    state = toml.load(os.path.expanduser(config["base"]["state_file"]))["state"]

    # Now you have the identifiers of the CloudFormation resources loaded,
    # Set up your S3 Bucket for the Document Bucket
    bucket = boto3.resource("s3").Bucket(state["DocumentBucket"])
    # Set up your DynamoDB Table for the Document Bucket
    table = boto3.resource("dynamodb").Table(state["DocumentTable"])
    # Pull configuration of KMS resources
    # MULTI-CMK-COMPLETE: Configure Walter
    faythe_cmk = state["FaytheCMK"]
    walter_cmk = state["WalterCMK"]
    # And the Master Key Provider configuring how to use KMS
    # MULTI-CMK-COMPLETE: Add Walter to the CMKs to Use
    cmk = [faythe_cmk, walter_cmk]
    mkp = aws_encryption_sdk.KMSMasterKeyProvider(key_ids=cmk)

    # Set up the API to interact with the Document Bucket using all these resources
    operations = DocumentBucketOperations(bucket, table, mkp)

    return operations
