To run this project you will need:
- Java 21
- mvn 3.9.11
- MiKTex with the packages of pdflatex (pdflatexpicscale, pdflatexpicscale_doc, pdflatexpicscale_source, etc)


This project is already compiled, so you can just clone the repo using "git clone https://github.com/LucasdeLyra/underroot.git" or download the .zip and then follow runing the server and client inside the underroot folder

## Run server and client using .jar

java -jar server\target\Server.jar

client\target\Client.jar

## Run server and client using mvn

mvn -f server/pom.xml exec:java

mvn -f client/pom.xml exec:java

If you want you can build the server and client as follows:

## Build server and client 

mvn -f pom.xml clean install

