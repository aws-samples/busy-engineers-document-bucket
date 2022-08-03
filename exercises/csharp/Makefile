bootstrap:
	sudo curl -L -o dotnet-install.sh https://dot.net/v1/dotnet-install.sh
	sudo chmod +x dotnet-install.sh
	./dotnet-install.sh
	sudo ln -s /home/ec2-user/.dotnet/dotnet /usr/bin/dotnet
	rm -f dotnet-install.sh
	dotnet --version
