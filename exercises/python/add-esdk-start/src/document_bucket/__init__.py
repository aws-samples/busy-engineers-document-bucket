# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import os

# ADD-ESDK-START Add the ESDK Dependency
import boto3  # type: ignore
import toml

from .api import DocumentBucketOperations
from .config import config


def initialize() -> DocumentBucketOperations:
    """
    Configure a Document Bucket API automatically with resources bootstrapped
    by CloudFormation.
    """
    # Load the pointers to CloudFormation resources that you just deployed
    state = toml.load(os.path.expanduser(config["base"]["state_file"]))["state"]

    # Now you have the identifiers of the CloudFormation resources loaded,
    # Set up your S3 Bucket for the Document Bucket
    bucket = boto3.resource("s3").Bucket(state["DocumentBucket"])
    # Set up your DynamoDB Table for the Document Bucket
    table = boto3.resource("dynamodb").Table(state["DocumentTable"])

    # ADD-ESDK-START Configure the Faythe KMS Key in the Encryption SDK

    # Set up the API to interact with the Document Bucket using all these resources
    operations = DocumentBucketOperations(bucket, table)

    return operations
