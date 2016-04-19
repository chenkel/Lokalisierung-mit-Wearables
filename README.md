# Lokalisierung mit Wearables

Lokalisierung mit Hilfe von WiFi-Fingerprinting, BLE Beacons und QR-Code auf Google Glass und Android Wear.

## Installation
1. Klone das Repository.  
	``` sh 
	git clone https://github.com/chenkel/Lokalisierung-mit-Wearables.git
	```
2. Öffne das heruntergeladene Projekt in Android Studio 2.0.

In dem Projekt befinden sich vier Module: **common**, **glass**, **mobile** und **wear**.
Gradle wird automatisch die benötigten Abhängigkeiten für die einzelnen Module herunterladen.

## Kompilieren und Starten

Wähle zunächst eine der drei Konfigurationen (*mobile*, *glass* oder *wear*) aus und starte das Programm mit **Run** in Android Studio. 

Dabei sollte das betreffende Gerät bereits vorher angeschlossen oder gepaired sein.

**Beachte für Google Glass** muss die *Run Configuration* angepasst werden. Dabei wählt man unter

*Launch Options* --> **Specified Activity** und dann 
*Activity* --> **smartwatch.context.project.activities.MainActivity**
	