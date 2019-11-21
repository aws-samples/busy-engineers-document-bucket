# Getting Started

## Background

In this section, you will prepare your AWS Environment to work with the Busy Engineer's Document Bucket. As you work through the workshop, you will gain familiarity with the core concepts required to use AWS KMS, the AWS Encryption SDK, and how to leverage features like multiple CMK support and Encryption Context to secure your application.

To prepare your environment, you will do the following:

1. Deploy a CloudFormation stack to launch a Cloud9 IDE
1. Launch the Cloud9 IDE that you will use to work with the rest of the workshop
1. Launch resource stacks using the AWS Cloud Development Kit (CDK), including
    * The `BusyEngineersDocumentBucket` stack, with your DynamoDB table and S3 bucket
    * One CMK in one region, called `Faythe`, that you will use in encryption and decryption operations
    * One CMK in another region, called `Walter`, that you will use in encryption and decryption operations
1. Bootstrap the development environment in Cloud9
1. Start the workshop!

## Make the Change

1. Sign in to your AWS Account for the workshop
    * Make sure this is **not** a production account! This is a workshop for learning and experimentation. Don't put production at risk!
1. [Click this link to load the CloudFormation template for your Cloud9 IDE](https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2#/stacks/quickcreate?templateUrl=https%3A%2F%2Fbusy-engineers-cfn.s3.us-east-2.amazonaws.com%2Fdocument-bucket-cloud9-bootstrap.yaml&stackName=BusyEngineersDocumentBucketEnvironment)
1. Click **Create Stack** to launch the stack.
1. [Open the Cloud9 Console](https://us-east-2.console.aws.amazon.com/cloud9/home?region=us-east-2#) to find your Cloud9 IDE.
1. Click **Open IDE** to launch Cloud9
    * The repository will check out to `~/environment/workshop` automatically
1. `cd ~/environment/workshop`
1. Execute `make bootstrap` to set up your workshop environment

## Try it Out

* `make bootstrap` will take approximately 5 minutes. Thank you for your patience while it sets up the environment for you.
    * If you are in a live workshop, this is good to run during the presentation.
    * If you are working on your own, grab a cup of your favorite beverage while you wait.
* After `make bootstrap` completes, you will have:
    1. Deployed your workshop stacks using CDK
    1. Language environments for all of the workshop languages
* Choose your language of choice for the workshop, and `cd` to its folder under `exercises`
    * `cd ~/environment/workshop/exercises/node-javascript` for NodeJS
    * `cd ~/environment/workshop/exercises/python` for Python
    * `cd ~/environment/workshop/exercises/java` for Java
* Time to start coding!

## Explore Further

* Check out the `~/workshop/cdk` directory to see how the workshop resources are described using CDK
* [Who are Faythe and Walter, anyway?](https://en.wikipedia.org/wiki/Alice_and_Bob#Cast_of_characters) And the other characters you may encounter as you explore.

# Next exercise

Now that you have your environment and language selected, you can [Add the Encryption SDK](./add-the-encryption-sdk.md).
