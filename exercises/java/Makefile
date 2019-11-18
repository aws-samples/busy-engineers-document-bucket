bootstrap:
	sudo yum install -y java-11-amazon-corretto-headless
	sudo update-alternatives --set java /usr/lib/jvm/java-11-amazon-corretto.x86_64/bin/java
	java -version
	wget http://apache.mirrors.lucidnetworks.net/maven/maven-3/3.6.2/binaries/apache-maven-3.6.2-bin.tar.gz -O /tmp/maven.tar.gz
	cd ~; tar xzvf /tmp/maven.tar.gz
	sudo update-alternatives --install /usr/bin/mvn mvn ~/apache-maven-3.6.2/bin/mvn 0
	mvn -version
