
bootstrap: | install_node node_modules

install_node:
	@# Use latest Node
	@echo "node" > ~/.nvmrc
	@curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.1/install.sh | bash
	@NVM_DIR=/home/ec2-user/.nvm . "$$NVM_DIR/nvm.sh"; nvm install node
	echo 'export AWS_SDK_LOAD_CONFIG=1' >> ~/.bashrc
	. ~/.bashrc

node_modules: 
	find . -maxdepth 1 -type d \( ! -name . \) -exec bash -c "cd '{}' && npm ci" \;
