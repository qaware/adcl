# adcl
Automatic Dependency Change Log - project together with TH Bingen

### Configuration:   
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
                            <value>neo4j"</value>
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
                    </properties>
                </configuration>
            </plugin>
            ...
        </plugins>
    </build>
...
</project>
```

### Launch:  
````
mvn adcl:start  
````

#### Notes:
- The syntax of `.properties` files uses backslashes (`\`) for escaping characters like `\=` or `\n`. This causes invalid parsing with standard windows paths like `C:\Users` (will be read as `C:Users`). Use `C:/Users` or `C:\\Users` instead.
