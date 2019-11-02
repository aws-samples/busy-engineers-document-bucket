# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

pretty:
	prettier --no-config --write lib/**/*.ts
	prettier --no-config --write bin/*.ts

clean:
	find bin/ lib/ -name '*.js' -exec rm -v {} \;
	find bin/ lib/ -name '*.d.ts' -exec rm -v {} \;
