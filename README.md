# adcl
Automatic Dependency Change Log - project together with TH Bingen

#### Was muss der Application übergeben werden?  

###### Der Pfad zur Config Datei:  
````
java -jar adcl.jar configPath=C:\adcl\config.properties  
````
#### Was muss alles in der Config drinstehen?  

###### Die uri der Neo4j instance auf welcher der Changlog abgespeichert werden soll:  
spring.data.neo4j.uri=bolt://localhost:7687  

###### Der Nutzername und das Passwort um Zugriff auf die Datenbank zu beommen:  
spring.data.neo4j.username=neo4j  
spring.data.neo4j.password=Test  

###### Der Pfad zu dem kompilierten Projekt, da .class Dateien eingelesen werden:  
project.uri=C:\\\\Users\\\\Daniel Drießen\\\\Documents\\\\TestDaten\\\\classes  

###### Der Commit Name unter dem das Ergebnis zu finden ist:  
project.commit.current=chummer2 oder aber project.commit=chummer2  

#### Was kann alles in der Config zusätzlich konfiguriert werden?  

###### Welcher Commit kam vor Diesem, sodass ein Changelog zwischen beiden erstellt werden kann:  
project.commit.previous=chummer1  

###### Wo soll der Report abgelegt werden (lokaler Changelog)?  
report.destination=C:\\\\ADCLTest  

#### Wie sieht die Datei dann am Ende aus?  

###### So:   
```properties
spring.data.neo4j.uri=bolt://localhost:7687  
spring.data.neo4j.username=neo4j  
spring.data.neo4j.password=Test  
project.uri=C:\\\\Users\\\\Daniel Drießen\\\\Documents\\\\TestDaten\\\\classes  
project.commit.previous=chummer1  
project.commit.current=chummer2  
report.destination=C:\\\\ADCLTest  
```
