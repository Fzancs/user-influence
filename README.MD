# Simulation de l’influence des opinions en réseau
Développement d'un système distribué permettant aux utilisateurs
d’échanger et d’influencer leurs opinions sur divers sujets via un serveur
Gestion des interactions en réseau avec des rôles spécifiques (users,
proposers, influencers, critical thinkers) (Java, Sockets TCP/IP)

```
javac Server.java User.java Proposer.java Influencers.java CT.java CF.java 

java Server

java User 1
java Proposer --topic='need help ?'
java Influencers
java CT 
java CF
```
