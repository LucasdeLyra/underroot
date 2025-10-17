## Build server and client 

mvn -f pom.xml clean install

## Run server and client

mvn -f server/pom.xml exec:java

mvn -f client/pom.xml exec:java
