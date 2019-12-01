# Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

.NOTPARALLEL:

bootstrap: | install deploy state create_grants

install:
	npm ci

deploy:
	@echo "*** START CDK STACK DEPLOYMENT ***"
	npx cdk deploy BusyEngineersDocumentBucketStack --require-approval never
	npx cdk deploy BusyEngineersFaytheCMKStack --require-approval never
	npx cdk deploy BusyEngineersWalterCMKStack --require-approval never
	@echo "*** CDK STACK DEPLOYMENT COMPLETE ***"

state:
	@echo "*** START CDK STATE STORAGE ***"
	npx ts-node bin/cdk_state.ts
	@echo "*** CDK STATE STORAGE COMPLETE***"

create_grants:
	@echo "*** START CDK CREATE GRANTS ***"
	npx ts-node bin/create_grants.ts
	@echo "*** CDK CREATE GRANTS COMPLETE***"
	
revoke_grants: revoke_walter_grant revoke_faythe_grant

revoke_walter_grant:
	npx ts-node bin/revoke_walter_grants.ts

revoke_faythe_grant:
	npx ts-node bin/revoke_faythe_grants.ts

list_grants:
	npx ts-node bin/list_grants.ts

data_purge:
	npx ts-node bin/data_purge.ts
