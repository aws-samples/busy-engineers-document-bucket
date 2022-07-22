# Thank You!

Thank you for working through the workshop today. We hope that you found it to be useful in getting started with client-side encryption using the AWS Encryption SDK and AWS KMS.

# Cleaning Up

There are helper scripts available to help you completely tear down your stack resources.

## Emptying Your Document Bucket

To remove the contents of your test S3 bucket so that CloudFormation can completely delete the stack, do the following:

```bash
cd ~/environment/workshop/cdk
make data_purge
```

## Deleting Your Document Bucket Resource Stacks

After emptying your Document Bucket, you can now use CDK to destroy your stack.

```bash
cd ~/environment/workshop/cdk
npx cdk destroy BusyEngineer*
```

## Shutting Down Cloud9

Use the CloudFormation console to delete your Cloud9 stack.

1. <a href="https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2#" target="_blank">Open the CloudFormation Console</a>
1. Select the "BusyEngineersDocumentBucketEnvironment" stack
1. Press "Delete"

## Finished!

That's it! Your workshop resources have been torn down. Use the CloudFormation console in both regions to confirm your resources are all successfully cleaned up.

* <a href="https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2#" target="_blank">CloudFormation in us-east-2 (Cloud9, Document Bucket resources, Faythe)</a>
* <a href="https://us-east-1.console.aws.amazon.com/cloudformation/home?region=us-east-1#" target="_blank">CloudFormation in us-east-1 (Walter)</a>

# Feedback

We welcome comments, questions, concerns, contributions, and feature requests [on our GitHub page for the Busy Engineer's Document Bucket](https://github.com/aws-samples/busy-engineers-document-bucket/).

If there is content that can be improved or anything that you would like to see, we would like to cover it for you.

At AWS Cryptography, our mission is to make tools that are easy to use, hard to misuse, and that help our customers protect their most sensitive data wherever and whenever it is.

We look forward to hearing from you about this workshop or your needs.

Thank you again for your time, and go forth and be secure!
