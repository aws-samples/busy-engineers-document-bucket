// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import s3 = require("@aws-cdk/aws-s3");
import cf = require("@aws-cdk/aws-cloudfront");
import iam = require("@aws-cdk/aws-iam");
import cognito = require("@aws-cdk/aws-cognito");

export class WebappStack extends cdk.Stack {
  constructor(
    scope: cdk.Construct,
    id: string,
    props: cdk.StackProps,
    config: Record<string, any>
  ) {
    super(scope, id, props);

    // Set up a Cognito User Pool and Identity Pool to set up authenticated website
    // access and IAM role credential distribution.
    const userPool = new cognito.UserPool(this, config.user_pool_name, {
      signInType: cognito.SignInType.USERNAME,
      userPoolName: config.user_pool_name
    });

    const identityPool = new cognito.CfnIdentityPool(
      this,
      config.identity_pool_name,
      {
        allowUnauthenticatedIdentities: false,
        identityPoolName: config.identity_pool_name
      }
    );

    const userPoolClient = new cognito.UserPoolClient(
      this,
      config.user_pool_client_name,
      {
        userPool: userPool
      }
    );

    // Design source: https://aws.amazon.com/premiumsupport/knowledge-center/cloudfront-serve-static-website/ and
    // https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html

    // Create an Origin Access Identity to attach to CF Distribution and use in bucket
    // policy to grant CF permission.
    // See also: https://github.com/aws/aws-cdk/issues/941
    const cfOAID = new cf.CfnCloudFrontOriginAccessIdentity(
      this,
      config.oaid_name,
      {
        cloudFrontOriginAccessIdentityConfig: {
          comment: config.oaid_comment
        }
      }
    );

    // To use CloudFront Origin Identity, the "website bucket" is a regular bucket,
    // not a S3 static website bucket.
    const webappBucket = new s3.Bucket(this, config.bucket_name, {
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      encryption: s3.BucketEncryption.KMS_MANAGED
    });

    // Build a policy to allow the OAID access to the bucket.
    // https://docs.aws.amazon.com/AmazonS3/latest/dev/example-bucket-policies.html#example-bucket-policies-use-case-6
    const bucketPolicy = new iam.PolicyStatement();
    bucketPolicy.addActions("s3:GetObject");
    bucketPolicy.addResources(webappBucket.bucketArn + "/*");
    bucketPolicy.addCanonicalUserPrincipal(cfOAID.attrS3CanonicalUserId);

    // Attach the policy to the bucket
    webappBucket.addToResourcePolicy(bucketPolicy);

    // Actually create the distribution, attaching the OAID and the Bucket
    const webDistribution = new cf.CloudFrontWebDistribution(
      this,
      config.distribution_name,
      {
        originConfigs: [
          {
            s3OriginSource: {
              s3BucketSource: webappBucket,
              originAccessIdentityId: cfOAID.ref
            },

            behaviors: [{ isDefaultBehavior: true }]
          }
        ]
      }
    );

    // Outputs
    const outputs = config.outputs;
    const userPoolExport = new cdk.CfnOutput(this, outputs.user_pool_name, {
      value: userPool.userPoolArn,
      exportName: outputs.user_pool_name
    });
    const identityPoolExport = new cdk.CfnOutput(
      this,
      outputs.identity_pool_name,
      {
        value: identityPool.ref,
        exportName: outputs.identity_pool_name
      }
    );
    const userPoolClientExport = new cdk.CfnOutput(
      this,
      outputs.user_pool_client_name,
      {
        value: userPoolClient.userPoolClientId,
        exportName: outputs.user_pool_client_name
      }
    );
  }
}
