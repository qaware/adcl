# adcl
Automatic Dependency Change Log - project together with TH Bingen

### Configuration:   
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
# Folder where report data will be placed  
report.destination=path/to/report  
```

### Launch:  
````
java -jar adcl.jar configPath=C:/adcl/config.properties  
````

#### Notes:
- The syntax of `.properties` files uses backslashes (`\`) for escaping characters like `\=` or `\n`. This causes invalid parsing with standard windows paths like `C:\Users`. Use `C:/Users` or `C:\\Users` instead.
