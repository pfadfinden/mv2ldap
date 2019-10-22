#  BdP MV :left_right_arrow: LDAP Synchronisation

Java Applikation zur automatisierten Synchronisation von Benuterdaten zwischen Mitgliederverwaltung und LDAP 
Benutzerverzeichnis des Bund der Pfadfinderinnen und Pfadfinder e.V. (BdP).


## Hilfe & Kontakt
Fehler und Verbesserungsvorschläge können gerne als [Github Issue](https://github.com/pfadfinden/mv2ldap/issues) 
oder Pull Request eingestellt werden. Wir freuen uns über Mitwirkung!

Kontakt: hilfe@pfadfinden.de

## Ausfuehrung

```
# Normale Ausfuehrung der LDAP Synchronisation
java -jar -Dspring.profiles.active=prod ldap-sync-1.2.0.jar 

# Zusaetzlich mit Ausgabe von verwaisten Benutzern (in keiner Berechtigungsgruppe) in LDAP Verzeichnis
java -jar -Dspring.profiles.active=prod ldap-sync-1.2.0.jar --orphaned
```
