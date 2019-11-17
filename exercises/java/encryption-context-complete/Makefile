coverage:
	mvn clean
	mvn test
	cd ./target/site/jacoco/; python -m http.server &
	elinks http://localhost:8000
	killall python
