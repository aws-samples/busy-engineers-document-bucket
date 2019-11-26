
bootstrap: | bootstrap_languages bootstrap_cdk bootstrap_done

bootstrap_cdk:
	$(MAKE) -C cdk bootstrap

bootstrap_languages:
	$(MAKE) bootstrap_node_javascript bootstrap_python bootstrap_java -j 3
	$(MAKE) bootstrap_node_typescript

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
