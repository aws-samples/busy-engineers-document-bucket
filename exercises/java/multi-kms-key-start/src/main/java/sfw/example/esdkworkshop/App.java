// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package sfw.example.esdkworkshop;

import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Entry point for writing logic to work with the Document Bucket, with a helper to obtain an API
 * instance to use.
 */
public class App {

  // The names of resources from the configuration file must exactly match those
  // keys for the automatic mapping.
  // CHECKSTYLE:OFF AbbreviationAsWordInName

  /**
   * Obtain a Document Bucket API initialized with the resources as configured by the bootstrap
   * configuration system.
   *
   * @return a new {@link Api} configured automatically by the bootstrapping system.
   */
  public static Api initializeDocumentBucket() {
    // Load the TOML State file with the information about launched CloudFormation resources
    StateConfig stateConfig = new StateConfig(Config.contents.base.state_file);

    // Configure DynamoDB client
    String tableName = stateConfig.contents.state.DocumentTable;
    AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.defaultClient();

    // Configure S3 client
    String bucketName = stateConfig.contents.state.DocumentBucket;
    AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    // Load configuration of KMS resources
    String faytheKmsKey = stateConfig.contents.state.FaytheKmsKey;
    // MULTI-KMS-KEY-START: Configure Walter

    // Set up the Master Key Provider to use KMS
    // MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
    KmsMasterKeyProvider mkp = KmsMasterKeyProvider.builder().buildStrict(faytheKmsKey);

    return new Api(ddbClient, tableName, s3Client, bucketName, mkp);
  }
  // CHECKSTYLE:ON AbbreviationAsWordInName

  /**
   * Entry point for writing logic to interact with the Document Bucket system.
   *
   * @param args the command-line arguments to the Document Bucket.
   */
  public static void main(String[] args) {
    // Interact with the Document Bucket here or in jshell (mvn jshell:run)
  }
}
