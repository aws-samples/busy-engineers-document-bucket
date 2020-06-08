
bootstrap: | install_node node_modules

install_node:
	@# Use latest Node
	@echo "node" > ~/.nvmrc
	@curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.1/install.sh | bash
	@NVM_DIR=/home/ec2-user/.nvm . "$$NVM_DIR/nvm.sh"; nvm install node
	echo "export AWS_REGION=$$(curl http://169.254.169.254/latest/meta-data/placement/availability-zone 2>/dev/null | sed 's/.$$//')" >> ~/.bashrc
	. ~/.bashrc

node_modules: 
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm ci"

basic_cli_smoke_tests:
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.js list"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.js store ./store.js -c context:value -c key:value"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.js list"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.js search context"

npm_update:
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm install"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm update"

npm_audit:
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm audit"
