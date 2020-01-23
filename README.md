# adcl
Automatic Dependency Change Log - project together with TH Bingen

### Configuration:   
If tool is started as a plugin your pom.xml should look like this:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
...
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>de.thbingen</groupId>
                <artifactId>adcl</artifactId>
                <version>1.0-SNAPSHOT</version>
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
                            <value>test</value>
                        </property>
                        <!-- Folder where .class files of the project are located  -->
                        <property>
                            <name>project.uri</name>
                            <value>path/to/project/classes</value>
                        </property>
                        <!-- Name to store new analysed data -->
                        <property>
                            <name>project.commit.current</name>
                            <value>test2</value>
                        </property>
                        <!-- (Optional) Name for last data to create a changelog against -->
                        <property>
                            <name>project.commit.previous</name>
                            <value>versionRef2</value>
                        </property>
                        <!-- (Optional) Every missing variable can also be loaded from a file. See properties definition -->
                        <property>
                            <name>configPath</name>
                            <value>path/to/config</value>
                        </property>
                    </properties>
                </configuration>
            </plugin>
            ...
        </plugins>
    </build>
...
</project>
```

If you want to start ADCL as a jar it needs either a file which looks like this:  
```properties
# Database connection  
spring.data.neo4j.uri=bolt://localhost:7687
spring.data.neo4j.username=neo4j
spring.data.neo4j.password=neo4j
# Folder where .class files of the project are located  
project.uri=path/to/project/classes
# Name for last data to create a changelog against
project.commit.previous=versionRef1
# Name to store new analysed data  
project.commit.current=versionRef2
```
or you can launch it by giving ADCL the informations directly via the commandline.  
You can also decide to use a mix of everything.  

### Install plugin as a 3rd party JAR  
```shell script
mvn install:install-file -Dfile="path/to/adcl-thin.jar" -DgroupId="de.thbingen" -DartifactId="adcl" -Dversion="1.0-SNAPSHOT" -Dpackaging="maven-plugin"
```
### Launch:  
As a plugin with thin jar:  
```shell script
mvn adcl:start  
```
with arguments (arguments must start with prefix `adcl.`):
```shell script
mvn adcl:start -Dadcl.spring.data.neo4j.uri=bolt://localhost:7687 -Dadcl.spring.data.neo4j.username=neo4j ...
```
with arguments (with prefix) and file:
```shell script
mvn adcl:start -Dadcl.configPath=C:/adcl/config.properties -Dadcl.spring.data.neo4j.uri=bolt://localhost:7687 ...
```
or as a fat jar via config file:
```shell script
java -jar adcl.jar configPath=C:/adcl/config.properties
```
or as a fat jar by giving the informations as arguments:
```shell script
java -jar adcl.jar spring.data.neo4j.uri=bolt://localhost:7687 spring.data.neo4j.username=neo4j spring.data.neo4j.password=neo4j ...                                                 
```
or as a fat jar as a mix of file and arguments:
```shell script
java -jar adcl.jar configPath=C:/adcl/config.properties spring.data.neo4j.uri=bolt://localhost:7687 spring.data.neo4j.username=neo4j spring.data.neo4j.password=neo4j                                                
```
Priority is (in descending priority) POM/System.properties->CLI-Arguments->File  

#### Notes:
- The syntax of `.properties` files uses backslashes (`\`) for escaping characters like `\=` or `\n`. This causes invalid parsing with standard windows paths like `C:\Users` (will be read as `C:Users`). Use `C:/Users` or `C:\\Users` instead.
