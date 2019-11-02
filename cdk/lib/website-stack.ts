// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import cdk = require("@aws-cdk/core");
import s3 = require("@aws-cdk/aws-s3");
import cf = require("@aws-cdk/aws-cloudfront");
import iam = require("@aws-cdk/aws-iam");

export class WebsiteStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // Design source: https://aws.amazon.com/premiumsupport/knowledge-center/cloudfront-serve-static-website/ and
    // https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html

    // Create an Origin Access Identity to attach to CF Distribution and use in bucket
    // policy to grant CF permission.
    // See also: https://github.com/aws/aws-cdk/issues/941
    const cfOAID = new cf.CfnCloudFrontOriginAccessIdentity(
      this,
      "BusyEngineersWebsiteCFOAID",
      {
        cloudFrontOriginAccessIdentityConfig: {
          comment: "BusyEngineersWebsiteCFtoBucket"
        }
      }
    );

    // To use CloudFront Origin Identity, the "website bucket" is a regular bucket,
    // not a S3 static website bucket.
    const websiteBucket = new s3.Bucket(this, "BusyEngineersWebsiteBucket", {
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      removalPolicy: cdk.RemovalPolicy.DESTROY
    });

    // Build a policy to allow the OAID access to the bucket.
    // https://docs.aws.amazon.com/AmazonS3/latest/dev/example-bucket-policies.html#example-bucket-policies-use-case-6
    const bucketPolicy = new iam.PolicyStatement();
    bucketPolicy.addActions("s3:GetObject");
    bucketPolicy.addResources(websiteBucket.bucketArn + "/*");
    bucketPolicy.addCanonicalUserPrincipal(cfOAID.attrS3CanonicalUserId);

    // Attach the policy to the bucket
    websiteBucket.addToResourcePolicy(bucketPolicy);

    // Actually create the distribution, attaching the OAID and the Bucket
    const webDistribution = new cf.CloudFrontWebDistribution(
      this,
      "BusyEngineersWebsiteCF",
      {
        originConfigs: [
          {
            s3OriginSource: {
              s3BucketSource: websiteBucket,
              originAccessIdentityId: cfOAID.ref
            },

            behaviors: [{ isDefaultBehavior: true }]
          }
        ]
      }
    );
  }
}
