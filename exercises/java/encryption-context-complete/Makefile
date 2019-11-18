coverage:
	# This is a convenience target for workshop developers, not needed for students
	mvn clean
	mvn test
	cd ./target/site/jacoco/; python3 -m http.server &
	elinks http://localhost:8000
	killall python3
