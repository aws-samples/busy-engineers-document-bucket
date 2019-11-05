# Busy Engineer's Document Bucket - Development Resources

These are tools, utilities, and documentation used to build and update the Busy
Engineer's Document Bucket. These are not needed for running the workshop or working
through the tutorials.

## cloud9-generator

CDK App to synthesize the template that workshop students launch. This kicks off a 
Cloud9 IDE with the repository checked out. `make cloud9` synthesizes and uploads to
the Busy Engineer's CFN S3 bucket, permissions permitting.
