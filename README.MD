# Kafka Streams Test Applikation

Dieses Projekt wurde von [sfnipt](https://github.com/sfnipt)/[silfank](https://github.com/silfank) und [tisuter](https://github.com/tisuter) erstellt im Rahmen eines Techbiers.
Dieses Repo hier dient nur als Übergangskopie. Das ursprüngliche Techbier-Repo enthält zudem die Aufgaben, die im Techbier gelöst wurden. Dieses Repo hat nur die Lösungen.
Ursprüngliches Repo: [techbier-repo](https://github.com/sfnipt/techbier-kafka-streams)

## Kafka Cluster
Im Projekt bfinden sich zwei docker-compose files um sich lokal einen Cluster installieren zu können. Es stehen zwei Distributionen zur Auswahl:
- RedPanda
- Confluent Kafka

## Java Applikation
Die Applikation beinhaltet Producer und Consumer für zwei Quell-Topics:  
- transactions: Dieses Topic enthält Daten des Typs "Payment" im Avro-Format. Es wird im 2sek Rythmus eine Nachricht generiert
- accounts: Dieses Topic enthält Daten des Typs "Account" im Avro-Format. Sie werden jeweils beim Aufstarten der Applikation einmalig geladen. Das Topic sollte wenn möglich "compacted" angelegt werden.

Im Ordner "streams" befinden sich mehrere Kafka-Streams Anwendungsfälle. Um sie zu aktiveren muss das @Component einkommentiert werden. Die benötigten Topics werden auf dem Cluster automatisch angelegt.

## GitOps
Es sind zwei unterschiedliche Tool vorhanden, um den Status des Clusters zu definieren (Topic-Management, ACLs, ...)
- [JulieOps](https://julieops.readthedocs.io/en/3.x/)
- [Topicctl](https://segment.com/blog/easier-management-of-Kafka-topics-with-topicctl/)

# Setup

Folgendes ausführencd .
```
mvn clean install
```
## Cluster aufsetzen
Im Ordner `src/main/resources/docker` befinden sich je ein Unterordner für Confluent Kafka oder Red Panda. Sie beinhalten je ein docker-compose.yml, durch welches sich ein Cluster aufsetzen lässt. Um den jeweiligen Cluster zu initialisieren, muss auf der Kommandozeile im jeweiligen Ordner `docker-compose up -d` ausgeführt werden. Für IntelliJ-Nutzer sind die run configurations bereits vorhanden.  
Es dauert relativ lange, bis der Cluster komplett aufgestartet ist. Das Control Center bei Confluent ist unter http://localhost:9021/clusters aufrufbar.

# Java App starten

## 1. Config setzen
In `src/main/resources/application.yaml` folgende Werte befüllen:
- bootstrap-servers 
- schema.registry.url

## 2. App starten
Die Klasse `ch.ipt.kafka.KafkaStreamsApplication` starten



