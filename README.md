# Lokalisierung mit Wearables

Lokalisierung mit Hilfe von WiFi-Fingerprinting, BLE Beacons und QR-Code auf Google Glass und Android Wear.

## Installation
1. Klone das Repository.
	``` sh 
	git clone https://github.com/chenkel/Lokalisierung-mit-Wearables.git
	```
	
2. Öffne das heruntergeladene Projekt in Android Studio 2.0.
3. Gradle wird automatisch die benötigten Abhängigkeiten für die einzelnen Module herunterladen.
4. In dem Projekt befinden sich nun die vier Module:
    1. **common**
    2. **glass**
    3. **mobile**
    4. **wear**.


## Kompilieren und Starten
1. Das betreffende Gerät sollte bereits vorher angeschlossen und/oder gepaired sein.

2. Wähle dann eine der drei Konfigurationen aus:

    ***glass:*** Die Glassware für Google Glass.

    ***mobile:*** Die App für Android  Smartphones zum schnellen Testen.

    ***wear:***  Die App für Android Wear (getestet auf Motorola Moto 360).

3. Starte im Anschluss die App mit dem Befehl **Run** in Android Studio.

	
## Debugging von Android Wear Geräten (wie z. B. Moto 360) über Bluetooth
1. Installiere Android Wear App auf einem anderen Android Gerät (Smartphone od. Tablet).
2. Kopple beide Geräte.
3. Aktiviere den Entwicklermodus für beide Geräte (also der Uhr und des Smartphones/Tablets)  durch 7-maliges Tippen auf die Build-Version in den Infos.
4. Aktiviere *Debugging over Bluetooth* in den Entwickleroptionen der Uhr und der Android Wear App auf dem anderen Gerät.
5. Öffne ein Terminal im Projekt in Android Studio und gib die folgenden zwei Befehle ein:
``` sh 
adb forward tcp:4444 localabstract:/adb-hub    
``` 	
``` sh 
adb connect 127.0.0.1:4444
```
Beachte den **Dialog** *Debugging zulasse...* auf der Uhr mit **OK** oder besser noch mit **Von diesem Computer immer zulassen** zu bestätigen.
6. Überprüfe abschließend unter den Einstellungen der Android Wear App, dass der Status in dem Abschnitt *Debugging über Bluetooth* wie folgt erscheint:
``` sh 
    Host: verbunden
``` 	
``` sh 
    Ziel: verbunden
```

