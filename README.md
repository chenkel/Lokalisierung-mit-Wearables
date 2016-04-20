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

