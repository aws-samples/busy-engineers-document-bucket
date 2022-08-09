# Getting Started

## Workshop Details
In this workshop, you will add encryption and decryption features to the Busy Engineer's Document Bucket to learn about some real world AWS patterns for integrating client-side encryption using AWS Key Management Service (AWS KMS) and the AWS Encryption SDK (ESDK) in application code. You will learn how to leverage features like multiple AWS KMS Key support and Encryption Context to secure your application.

To begin, the Document Bucket supports storing objects (documents or files or other blobs of data) in a private Amazon S3 bucket, and indexing them in Amazon DynamoDB. This allows Document Bucket users to share objects with other users, and store them for retrieval later. The DynamoDB entries provide metadata context for each Document Bucket object, as well as fast lookups to the provided context.

This context allows users to store additional information about the S3 object like the origin user, the destination fleet, the project, or any other tag that would be useful to know without having to download and examine the object.

DynamoDB is configured to allow you to index on the keys associated with the S3 objects. So, for example, it's a quick query to find out which objects have been tagged with metadata context that includes "configuration".

The Document Bucket supports the following APIs:

* `list`: This operation queries DynamoDB for all entries for all objects in the Document Bucket, and their metadata. It returns the `set` of items that have been stored.
* `store`: This operation accepts a blob of bytes and a `map` of metadata context. It generates a unique identifier (UID) for the object. The UID and associated metadata are written to DynamoDB. The bytes of the object are written to S3 under a key of that UID. Any context metadata keys in DynamoDB are updated to include that new UID.
* `retrieve`: This operation accepts a unique identifier (UID) as an argument. It looks up the UID in DynamoDB to identify the associated context and then retrieves the referenced object from S3. It returns the Document Bundle (UID, context, and referenced object) to the caller.
* `search`: This operation accepts a metadata key as search input. It queries DynamoDB for the `set` of objects in the Document Bucket with context matching that key. It returns the `set` of UIDs and their associated metadata.
    * Once you have identified the desired object(s), you can pass the UIDs to `retrieve` to fetch the objects.

This is a start for sharing, storing, and searching various objects. 

But what about sensitive documents? Or protecting, say, important configuration files from accidental corruption during storing or retrieving? 

The workshop exercises will help you address some of these challenges.

## Background

In this section, we will give you step-by-step instructions to prepare your AWS Environment to work with the Busy Engineer's Document Bucket. 

To set up your environment, you will:

* Deploy a CloudFormation stack to launch an AWS Cloud9 integrated development environment (IDE)
   * AWS Cloud9 lets you write, run, and debug your code with just a browser
* Launch the Cloud9 IDE that you will use for the rest of the workshop
* Launch resource stacks using the AWS Cloud Development Kit (CDK), including:
    * The `BusyEngineersDocumentBucket` stack, with your DynamoDB table and S3 bucket
    * One KMS Key in one region, called Faythe, that you will use in encryption and decryption operations
    * One KMS Key in another region, called Walter, that you will use in encryption and decryption operations
* Bootstrap the development environment in Cloud9

## Let's Go!

### Important Note About Accounts

If you are using your own AWS Account for this workshop, make sure:

1. It is not a production account. This is a workshop for learning and experimentation. Don't put production at risk!
1. When you are done with the exercises, you follow the instructions in [Clean Up and Closing](./clean-up-and-closing.md) to clean up the deployed resources.

If you are working through these exercises in an AWS classroom environment, AWS accounts have been created for you.

### Procedure

1. Sign in to your AWS Account for the workshop
1. Click this link to <a href="https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2#/stacks/quickcreate?templateUrl=https%3A%2F%2Fbusy-engineers-cfn.s3.us-east-2.amazonaws.com%2Fdocument-bucket-cloud9-bootstrap.yaml&stackName=BusyEngineersDocumentBucketEnvironment" target="_blank">load the CloudFormation template for your Cloud9 IDE</a>
    * The above link should put you in the US East (Ohio) region
    * Do not change your region settings from the default and do not switch regions in the console
1. Click **Create Stack** to launch the stack
    * It will take about three minutes to launch your Cloud9 IDE
1. Click this link to <a href="https://us-east-2.console.aws.amazon.com/cloud9/home?region=us-east-2#" target="_blank">Open the Cloud9 Console and find your Cloud9 IDE</a>
    * You may need to wait a minute and refresh while CloudFormation spins up the resources
    * There will be a blue tile titled ***BusyEngineersCloud9IDE*** when it's ready
1. At the bottom of the tile, click **Open IDE** to launch Cloud9
1. Type `cd ~/environment/workshop` and hit `Enter`
1. Type `make bootstrap` and hit `Enter` to set up your workshop environment
    * `make bootstrap` will take approximately 10 minutes to complete
    * While you wait, you may get started reading the first exercise, [Adding the Encryption SDK](./adding-the-encryption-sdk.md)
    * You can even go ahead and start making code changes as per the exercise instructions, but you'll need to wait for the bootstrap to complete before running any code
    * Keep an eye on the terminal running the bootstrap, and once it completes come back to these steps.
1. Once the bootstrap completes you will see `*** BOOTSTRAP COMPLETE ***`
    * At this point you have deployed your workshop stacks using CDK and set up language environments for all of the workshop languages
1. In Cloud9, close your Terminal window and open a new one (`Window -> New Terminal`) to pick up the changes `make bootstrap` installed
1. Choose your workshop language, and `cd` to its folder under `exercises`

=== "Java"

    ```bash 
    cd ~/environment/workshop/exercises/java
    ```

=== "Typescript Node.JS"

    ```bash 
    cd ~/environment/workshop/exercises/node-typescript
    ```

=== "JavaScript Node.JS"

    ```bash
    cd ~/environment/workshop/exercises/node-javascript
    ```

=== "Python"

    ```bash
    cd ~/environment/workshop/exercises/python
    ```

=== "C#"

    ```bash
    cd ~/environment/workshop/exercises/dotnet
    ```

**Your environment is ready!** 


# Start the workshop!

Now that you have your environment and language selected, you can start [Adding the Encryption SDK](./adding-the-encryption-sdk.md).
