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
  <version>1.0.2.SNAPSHOT</version>
</dependency>
```

Using Spring Boot 1.3+, you can simply write a Spring Boot App that looks like this:

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

## Available Customizations
If you have a bean of type `RestTemplate` in your application context, the underlying utility will use it,
instead of a direct `RestTemplate`.

By default, the app will sort entries, so that the yaml returned will have newest entries first. 
If you create a bean of type `Comparator<String>` with name `versionOrdering`, the app will use it
to sort entries.
