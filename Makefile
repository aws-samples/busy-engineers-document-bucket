
bootstrap: | bootstrap_languages bootstrap_cdk

bootstrap_cdk:
	$(MAKE) -C cdk bootstrap	

bootstrap_languages: 
	$(MAKE) bootstrap_node_javascript bootstrap_python bootstrap_java -j 3

bootstrap_node_javascript: 
	$(MAKE) -C exercises/node-javascript bootstrap

bootstrap_python:
	$(MAKE) -C exercises/python remove
	$(MAKE) -C exercises/python bootstrap

bootstrap_java: 
	$(MAKE) -C exercises/java bootstrap
