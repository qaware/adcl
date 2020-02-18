adcl
====
[![](https://jitpack.io/v/qaware/adcl.svg)](https://jitpack.io/#qaware/adcl)  
Automatic Dependency Change Log - project together with TH Bingen

This tool generates changelogs between two java project versions.

## Requirements
- A Neo4j Database
- The class files of the project you want to analyse

## How to use:  
### As maven plugin (recommended)
If you want to use ADCL as a maven plugin your pom.xml should look like this:
```xml
<project>
    
    <pluginRepositories>
        <pluginRepository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </pluginRepository>
    </pluginRepositories>
    
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.qaware</groupId>
	            <artifactId>adcl</artifactId>
	            <version>${adcl.version}</version>
                <configuration>
                    <properties>
                        <!-- Database connection --> 
                        <property>
                            <name>spring.data.neo4j.uri</name>
                            <value>bolt://localhost:7687</value>
                        </property>
                        <property>
                            <name>spring.data.neo4j.username</name>
                            <value>neo4j</value>
                        </property>
                        <property>
                            <name>spring.data.neo4j.password</name>
                            <value>neo4j</value>
                        </property>
                        <!-- More optional parameters -->
                    </properties>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
Project gets analysed with the adcl:start goal.
### Without maven
Download the fat jar from the releases page. You need to specify more startup options.

## Configuration
### Configuration options
| key                        | description                                                      | default                                                            | required | required w/o maven | optional |
|----------------------------|------------------------------------------------------------------|--------------------------------------------------------------------|----------|--------------------|----------|
| spring.data.neo4j.uri      | host and port to the neo4j database                              | bolt://127.0.0.1:7687                                              |          |                    | X        |
| spring.data.neo4j.username | username for the neo4j database                                  | neo4j                                                              |          |                    | X        |
| spring.data.neo4j.password | password for the neo4j database                                  |                                                                    | X        |                    |          |
| project.name               | the project name to persist in database                          | the artifactId specified in your pom                               |          | X                  |          |
| project.uri                | root folder for the class files                                  | the classes output specified in your pom (default: target/classes) |          | X                  |          |
| project.pom                | location of the project pom.xml if using maven                   | ./pom.xml                                                          |          |                    | X        |
| project.commit.previous    | the version name of the latest version in the database           | the latest version in the database                                 |          |                    | X        |
| project.commit.current     | the version name of the current data set that should be analysed | the version specified in your pom                                  |          | X                  |          |
| configPath                 | a path to a .properties file to load further options from        | ./config.properties                                                |          |                    | X        |
| nomaven                    | disable maven pom analysis even if a pom is found                | false                                                              |          |                    | X        |
| local                      | only generate a changelog artifact, don't create a new version   | false                                                              |          |                    | X        |
| basedir                    | the working directory for the project                            | .                                                                  |          |                    | X        |
### Configuration methods
**Note**: Configuration methods shown below are ordered in priority
#### (Maven) pom.xml
Just add more properties to your pom.xml
```xml
<property>
    <name>option-key</name>
    <value>option-value</value>
</property>
```
#### JVM arguments
Options can be set before launch as JVM arguments, prepended with `adcl.`
This would translate to
```sh
mvn adcl:start -Dadcl.spring.data.neo4j.password=neo4j
```
when calling from CLI
#### Program arguments (w/o maven)
Options can be set as program arguments
```sh
java -jar adcl-fat.jar spring.data.neo4j.password="password with spaces"
```
#### Properties file
ADCL searches for a file specified by the option `configPath` (or default `./config.properties`). If found it loads them as additional options.

#### Notes:
- Option values can be quoted for the case you want to add spaces into the value
- The syntax of `.properties` files uses backslashes (`\`) for escaping characters like `\=` or `\n`. This causes invalid parsing with standard Windows paths like `C:\Users` (will be read as `C:Users`). Use `C:/Users` or `C:\\Users` instead.
