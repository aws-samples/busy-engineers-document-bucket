# Busy Engineer's Document Bucket - Development Resources

These are tools, utilities, and documentation used to build and update the Busy
Engineer's Document Bucket. These are not needed for running the workshop or working
through the tutorials.

## cloud9-generator

CDK App to synthesize the template that workshop students launch. This kicks off a 
Cloud9 IDE with the repository checked out. `make cloud9` synthesizes and uploads to
the Busy Engineer's CFN S3 bucket, permissions permitting.

## Launching the stack for developers

This is an abbreviated version of the setup guide targeted toward people working on
the stack as developers rather than classroom students.

1. Launch the base CFN template (Cloud9) from the S3 bucket: https://busy-engineers-cfn.s3.us-east-2.amazonaws.com/document-bucket-cloud9-bootstrap.yaml
    * Convenience link: https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2#/stacks/quickcreate?templateUrl=https%3A%2F%2Fbusy-engineers-cfn.s3.us-east-2.amazonaws.com%2Fdocument-bucket-cloud9-bootstrap.yaml&stackName=BusyEngineersDocumentBucketEnvironment
    * Ensure you are using a role with admin privileges / sufficient privs to launch stacks and create CMKs/S3 buckets/CF distributions, etc.
1. Go to https://us-east-2.console.aws.amazon.com/cloud9/home?region=us-east-2# and launch the BusyEngineersCloud9IDE IDE
1. Bootstrap the CDK environment
    1. `cd workshop`
    1. `make bootstrap`


## License

This project is licensed under the Apache-2.0 License.
