bootstrap:
	wget https://d3pxv6yz143wms.cloudfront.net/11.0.5.10.1/java-11-amazon-corretto-devel-11.0.5.10-1.x86_64.rpm -O /tmp/java-corretto.rpm
	sudo yum install -y /tmp/java-corretto.rpm
	sudo update-alternatives --set java /usr/lib/jvm/java-11-amazon-corretto/bin/java
	java -version
	wget http://apache.mirrors.lucidnetworks.net/maven/maven-3/3.6.2/binaries/apache-maven-3.6.2-bin.tar.gz -O /tmp/maven.tar.gz
	cd ~; tar xzvf /tmp/maven.tar.gz
	sudo update-alternatives --install /usr/bin/mvn mvn ~/apache-maven-3.6.2/bin/mvn 0
	mvn -version
