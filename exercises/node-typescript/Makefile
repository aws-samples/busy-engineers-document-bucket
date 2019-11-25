
bootstrap: install_ts node_modules

install_ts-node:
	npm install ts-node -g

node_modules: 
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm ci"
