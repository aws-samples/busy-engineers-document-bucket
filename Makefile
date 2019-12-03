
bootstrap: | bootstrap_node_javascript bootstrap_environment bootstrap_done

# The 2 slowest tasks are the cdk standing up the stacks
# and the python install
bootstrap_environment:
	$(MAKE) bootstrap_cdk bootstrap_python bootstrap_java bootstrap_node_typescript resize -j 4

bootstrap_cdk:
	$(MAKE) -C cdk bootstrap

bootstrap_node_javascript:
	$(MAKE) -C exercises/node-javascript bootstrap

bootstrap_node_typescript:
	$(MAKE) -C exercises/node-typescript bootstrap

bootstrap_python:
	$(MAKE) -C exercises/python remove
	$(MAKE) -C exercises/python bootstrap

bootstrap_java:
	$(MAKE) -C exercises/java bootstrap

bootstrap_done:
	@echo "*** BOOTSTRAP COMPLETE ***"

# Smoke tests for the CLIs
basic_cli_smoke_tests:
	$(MAKE) -C exercises/node-javascript basic_cli_smoke_tests
	$(MAKE) -C cdk data_purge
	$(MAKE) -C exercises/node-typescript basic_cli_smoke_tests
	$(MAKE) -C cdk data_purge

resize:
	cdk/bin/resize.sh
