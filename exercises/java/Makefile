MAVEN_VERSION = "3.6.3"

bootstrap:
	sudo rpm --import https://yum.corretto.aws/corretto.key
	sudo curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
	sudo yum install -y java-11-amazon-corretto-devel
	sudo update-alternatives --set java /usr/lib/jvm/java-11-amazon-corretto/bin/java
	java -version
	wget http://mirror.cogentco.com/pub/apache/maven/maven-3/$(MAVEN_VERSION)/binaries/apache-maven-$(MAVEN_VERSION)-bin.tar.gz -O /tmp/maven.tar.gz
	cd ~; tar xzvf /tmp/maven.tar.gz
	sudo update-alternatives --install /usr/bin/mvn mvn ~/apache-maven-$(MAVEN_VERSION)/bin/mvn 0
	mvn -version
