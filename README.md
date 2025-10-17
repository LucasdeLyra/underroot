## Build server an client 

mvn -f server\pom.xml clean package
mvn -f client\pom.xml clean package

## Run server and client
mvn -f server\pom.xml exec:java -Dexec.mainClass="com.underroot.latexserver.Main"
mvn -f client\pom.xml exec:java -Dexec.mainClass="com.underroot.latexclient.Main"
