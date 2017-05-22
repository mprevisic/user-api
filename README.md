## Synopsis

This project represents a session and user management REST API as a showcase example of how to implement stateless authentication in Java using JWT tokens.

Used technogies are: Java 8, Spring Boot, Spring Security, Spring-Data-JPA, MySQL etc.

## Installation

Following prerequisites must be installed on your commputer to install and run the project:
- Java JRE 8
- Apache Maven (version 3.x)
- MySQL 5.x - (this is optional, the app can also be easily configured to run with H2 in-memory database instead if no persistent storage is required)

The deliverable of the application is a standalone ("fat") JAR containing an embedded Tomcat application server which is used to serve the application, so no additional application server is required to run the application.

To build the JAR, enter the following command in your command line while positioned in the root folder of the project:

>> mvn package

If build was successful, there should be a file named "user-api.jar" in folder /user-api/target/. Copy the file to a desired destination.
Before running the application, a configuration file named "application.properties" must be placed in the same folder where the user-api.jar is located. The configuration parameters of this file are described in the next chapter. After creating the application.properties file, enter following command to execute the application:

>> java -jar user-api.jar 

Alternatively, the application.properties file can be placed anywhere if running the app as follows:

>> java -Dspring.config.location=/some/location -jar user-api.jar 

## Configuration

Following configuration parameters are available in "application.properties": 

"spring.datasource.url" - URL connection string for connecting to the database
"spring.datasource.driverClassName" - class name of the JDBC driver used with the database
"spring.datasource.username" - username of the database user
"spring.datasource.password" - password of the database user
"server.port" - TCP port on which the API is available (defaults to 8080 if left empty)
"server.ssl.key-store" - path of the SSL keystore (required if using HTTPS)
"server.ssl.key-store-password" - password for the SSL keystore (required if using HTTPS)
"server.ssl.keyStoreType" - type of the SSL keystore (required if using HTTPS)
"server.ssl.keyAlias" - alias of the key in the SSL keystore used as HTTPS certificate (required if using HTTPS)

The following example shows a simple configuration which uses the H2 database and runs the service on port 8081:

spring.datasource.url=jdbc:h2:mem:users;DB_CLOSE_ON_EXIT=TRUE;
spring.datasource.driverClassName=org.h2.Driver
server.port=8080

The following example shows a configuration which uses the MySQL database and runs the service on port 8443 with HTTPS enabled:

spring.datasource.url=jdbc:mysql://localhost:3306/users
spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.name=userDS
spring.datasource.username=root
spring.datasource.password=
server.ssl.key-store=keystore.p12
server.ssl.key-store-password=abc123
server.ssl.keyStoreType=PKCS12
server.ssl.keyAlias=tomcat

An example key with the described properties (key-store = keystore.p12, password = abc123, type = PKCS12, key alias = tomcat) can be found under user-api/src/main/resources/keystore.p12

## API Reference

TBD

## Tests

The project contains following kinds of tests:
- Unit tests - simple tests which examine classes from the system in an isolated manner, implemented using JUJnit and Mockito library
- Functional tests (integration tests) - black-box tests which start the whole application and test it by executing REST API calls and checking returned response values and codes, cookies etc., implemented using JUnit integration for Spring Boot and REST Assured library for testing REST API endpoints

Unit tests are located in user-api/src/test/java/com/mprevisic/user/service and user-api/src/test/java/com/mprevisic/user/util. Functional tests are located in user-api/src/test/java/com/mprevisic/user/test/functional.

To execute unit tests, enter following command:

>> mvn test

To execute the functional tests enter:

>> mvn integration-test -P integration

(onls "mvn integration-test" will execute both unit tests and integration tests)

Upon executing the functional tests, a test report is generated in HTML format, which can be found at user-api/target/failsafe-reports/test-report.html.
