# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

pretty:
	prettier --no-config --write lib/**/*.ts
	prettier --no-config --write bin/*.ts
	prettier --no-config --write tsconfig.json

clean:
	find bin/ lib/ -name '*.js' -exec rm -v {} \;
	find bin/ lib/ -name '*.d.ts' -exec rm -v {} \;

cloud9:
	@echo "Development tool only! Workshop students do not need to run this target."
	@# synth does not catch if build errors have been introduced
	@npm run build
	cdk synth BusyEngineersCloud9Stack > ./document-bucket-cloud9-bootstrap.yaml
	aws s3 cp ./document-bucket-cloud9-bootstrap.yaml s3://busy-engineers-cfn --acl public-read
	rm ./document-bucket-cloud9-bootstrap.yaml

	@echo "https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2#/stacks/quickcreate?templateUrl=https%3A%2F%2Fbusy-engineers-cfn.s3.us-east-2.amazonaws.com%2Fdocument-bucket-cloud9-bootstrap.yaml&stackName=BusyEngineersDocumentBucketEnvironment"
