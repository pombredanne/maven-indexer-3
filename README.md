# maven-indexer

This application acts as a proxy for a Maven repository, presenting a YAML index appropriate for being consumed by
[http://www.cloudfoundry.org](Cloud Foundry) buildpacks to handle packaging dependencies.

## Building

`mvn clean install`

## Using in your app
Add to your pom.xml:

```xml
<dependency>
  <groupId>com.ecsteam.cloudfoundry</groupId>
  <artifactId>maven-indexer</artifactId>
  <version>1.0.1.RELEASE</version>
</dependency>
```

Using Spring Boot 1.3.2+, you can simply write a Spring Boot App that looks like this:

```java
package sample.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ecsteam.cloudfoundry.maven.indexer.EnableMavenIndexer;

@SpringBootApplication
@EnableMavenIndexer
public class SampleMavenIndexer {
	public static void main(String[] args) {
		SpringApplication.run(MavenIndexerAppApplication.class, args);
	}
}
```

Build your app and run with

`java -jar /path/to/your/boot.jar [--maven.repository.base=https://repo1.maven.org/maven2]`

By default, the app points to Maven Central
