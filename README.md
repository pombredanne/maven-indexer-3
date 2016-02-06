# maven-indexer

This application acts as a proxy for a Maven repository, presenting a YAML index appropriate for being consumed by 
[http://www.cloudfoundry.org](Cloud Foundry) buildpacks to handle packaging dependencies.

## Building

`mvn clean package`

## Running

`java -jar target/maven-indexer-1.0.0.RELEASE.jar [--maven.repository.base=https://repo1.maven.org/maven2]`

By default, the app points to Maven Central
