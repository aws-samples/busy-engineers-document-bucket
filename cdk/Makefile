# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

pretty:
	prettier --no-config --write lib/**/*.ts
	prettier --no-config --write bin/*.ts
	prettier --no-config --write tsconfig.json

clean:
	find bin/ lib/ -name '*.js' -exec rm -v {} \;
	find bin/ lib/ -name '*.d.ts' -exec rm -v {} \;

bootstrap:
	@# Use latest Node
	@echo "node" > ~/.nvmrc
	@curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.1/install.sh | bash
	@NVM_DIR=/home/ec2-user/.nvm . "$$NVM_DIR/nvm.sh"; nvm install node
	npm install -g typescript aws-cdk
	tsc --version
	cdk --version
	npm install
