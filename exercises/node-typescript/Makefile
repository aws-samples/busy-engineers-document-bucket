
bootstrap: install_ts-node node_modules

install_ts-node:
	npm install ts-node -g

node_modules: 
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm ci"

basic_cli_smoke_tests:
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.ts list"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.ts store ./src/store.ts -c context:value -c key:value"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.ts list"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} bash -c "cd '{}' && ./cli.ts search context"

npm_update:
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm install"
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm update"

npm_audit:
	find . -maxdepth 1 -type d \( ! -name . \) | xargs -I{} -P 4 bash -c "cd '{}' && npm audit"
	