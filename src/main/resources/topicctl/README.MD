#  Build vom topicctl Client

Da der Client plattformabhängig kompiliert ist, muss er gegebenenfalls gebildet werden:  
1. checkout https://github.com/segmentio/topicctl.git
2. go build cmd/topicctl/main.go
3. kopiere das gebildete Artefakt (main) in dieses Projekt in diesen Ordner
4. starte die Applikation von der Kommandozeile aus mit `./main apply --skip-confirm topology.yml --cluster-config cluster.yml `