## Busy Engineer's Document Bucket

This is the GitHub repo for the Busy Engineer's Document Bucket.
In this workshop,
you will be guided through adding client-side encryption with AWS KMS
and the AWS Encryption SDK.

The workshop instructions can be found at
https://document-bucket.awssecworkshops.com/

### Repository Structure

* [cdk](https://github.com/aws-samples/busy-engineers-document-bucket/tree/master/cdk) - the AWS Cloud Development Kit code describing all AWS resources required for the workshop.
* [dev](https://github.com/aws-samples/busy-engineers-document-bucket/tree/master/dev) - utilities for writing and updating the workshop, including the CDK resources for Cloud9 which is the base for launching workshop activities.
* [exercises](https://github.com/aws-samples/busy-engineers-document-bucket/tree/master/exercises) - the actual exercise source code for each supported language in the workshop.  `-start` is the content for actually working the exercise, and `-complete` is the finished exercise content, for reference.
* [instructions](https://github.com/aws-samples/busy-engineers-document-bucket/tree/master/instructions) - is the configuration and content for the documentation website, https://document-bucket.awssecworkshops.com

### Testing

After creating a PR,
test the changes by manually spinning up the workshop in a test account.
To spin up the workshop,
follow the instructions [here](https://document-bucket.awssecworkshops.com/getting-started/#important-note-about-accounts) until you reach the `make bootstrap` step.
Before running `make bootstrap`,
add your remote and check out your branch:
1. `git remote add [name] [httpUrl]`
1. `git fetch --all`
1. `git checkout [pr-branch-name]`

After the bootstrap has completed,
close the terminal and open a new one.
Then
1. `cd ~/environment/workshop` 
1. `make basic_cli_smoke_tests`

### Updating JavaScript dependencies

Make targets to update
and check the JavaScript dependencies exist.
Checking out the code locally
and running `make npm_update`
will run `npm update` on all JavaScript code.
Similarly `make npm_audit` can be used
to run `npm audit` on all JavaScript code.

## License

This project is licensed under the Apache-2.0 License.
