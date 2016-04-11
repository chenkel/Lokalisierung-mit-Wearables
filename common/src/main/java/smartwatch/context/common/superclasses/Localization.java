package smartwatch.context.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartwatch.context.common.helper.CalculationHelper;
import smartwatch.context.common.helper.WlanMeasurements;

public abstract class Localization extends CommonClass {
    private static final String TAG = Localization.class.getSimpleName();
    private List<WlanMeasurements> wlanMeasure = new ArrayList<>();
    private String priorPlaceId = "";

    /*timestamp*/
    long tstamp = 0;
    long diff;

    Integer[] bleRssi = {-200,-200,-200};

    String[] bluePlaces = {"1"};
    String[] redPlaces = {"2"};
    String[] yellowPlaces = {"3"};
    Boolean placesCleared = false;

    protected final BroadcastReceiver localizationScanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*Log.i(TAG, "localizationScanResultReceiver onReceive");*/

            /*Timestamping*/
            long tmp = tstamp;
            tstamp = System.currentTimeMillis();
            diff = tstamp - tmp;
            Log.i(TAG, "###new Timestamp: " + diff);

            List<ScanResult> currentResults = wifiManager.getScanResults();

            int measurementCount = currentResults.size();
            /*Log.i(TAG, "measurementCount: " + measurementCount);*/
            if (measurementCount > 0) {
                for (ScanResult result : currentResults) {
                    wlanMeasure.add(new WlanMeasurements(
                            result.BSSID,
                            result.level,
                            result.SSID,
                            0
                    ));
                }

                locateUser();
                wifiManager.startScan();

            } else {
                stopScanningAndCloseProgressDialog();
                Toast.makeText(context, "Keine APs in der Umgebung gefunden", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public Localization(Activity activity) {
        super(activity);
        setResultReceiver(localizationScanResultReceiver);
    }

    public void startLocalization() {
        outputList.clear();
        wlanMeasure.clear();

        /* Register Listener to collect results */
        getActivity().registerReceiver(localizationScanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        showLocalizationProgressOutput();
    }

    protected void showLocalizationProgressOutput(){
        progress = new ProgressDialog(getActivity());

        progress.setTitle("Lokalisierung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.w(TAG, "Dialog canceled");
                stopScanningAndCloseProgressDialog();
            }
        });
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }

    protected void locateUser() {

        Log.i(TAG, "Now in locate user");

        if (wlanMeasure.size() == 0) {
            Toast.makeText(getActivity(), "Bitte führen sie eine Messung durch", Toast.LENGTH_SHORT).show();
            stopScanningAndCloseProgressDialog();
        }

        //if(receivedBluetooth)
        //Hashmap mit Key Bluetooth Cell und Value Array aus Places    ("BeaconBlau"| [1130, 1132, 1134, 1336])
        //placeList = [1130, 1132, 1134, 1336]

        //else
        //placeList = measurementDB.getAllDistinctPlacesFromMeasurements();


                    /*Saves all places to placeList*/
        Cursor placeCursor = db.getAllDistinctPlacesFromMeasurements();
        if (placeCursor.getCount() == 0) {
            Toast.makeText(getActivity(), "Keine Messdaten vorhanden", Toast.LENGTH_SHORT).show();
            stopScanningAndCloseProgressDialog();
        }
        ArrayList<String> placeList = new ArrayList<>();
        for (placeCursor.moveToFirst(); !placeCursor.isAfterLast(); placeCursor.moveToNext()) {
            placeList.add(placeCursor.getString(0));
        }
        placeCursor.close();

        /*Anpassung der placeList abhängig von empfangenen Bluetooth Beacons*/
        /*if(receivedBluetooth){placeList = {1,2,3,4,5}*/

            Log.i(TAG, "BLE Rssi Size: " + bleRssi.toString());
            if (bleRssi[0] > -80) {
                if (!placesCleared) {
                    placeList.clear();
                }
                for (String place : bluePlaces) {
                    placeList.add(place);
                }
                placesCleared = true;
            }

            if (bleRssi[1] > -80) {
                if (!placesCleared) {
                    placeList.clear();
                }
                for (String place : redPlaces) {
                    placeList.add(place);
                }
                placesCleared = true;
            }

            if (bleRssi[2] > -80) {
                if (!placesCleared) {
                    placeList.clear();
                }
                for (String place : yellowPlaces) {
                    placeList.add(place);
                }
                placesCleared = true;
            }




                    /*Am Ende wird jedem Ort eine sse zugeordnet*/
        HashMap<String, Double> sseMap = new HashMap<>();
        List<WlanMeasurements> modellWerte = new ArrayList<>();

        for (String place : placeList) {
                        /*Get all BSSI and corresponding RSSI for place*/
            modellWerte.clear();

            /*Fills the cursor with all BSSIDs and their RSSIs at the place*/
            Cursor modelCursor = db.getAverageRssiByPlace(place);

                        /*Fügt in Model Data List dem Key bssi den Value rssi aus Datenbank hinzu
                        * bssi,rssi, ssid
                        */
            for (modelCursor.moveToFirst(); !modelCursor.isAfterLast(); modelCursor.moveToNext()) {
                WlanMeasurements messWert = new WlanMeasurements(modelCursor.getString(0),
                        modelCursor.getInt(1), modelCursor.getString(2));
                modellWerte.add(messWert);
            }
            modelCursor.close();

            double sseValue = CalculationHelper.calculateSse(wlanMeasure, modellWerte);

            /*If not in range of expected Bluetooth Beacon
            AND Sicherheitswert<20 THEN sseValue+20/modellWerte.size()
             */

            /*Log.i(TAG, " " + sseValue);*/
            sseMap.put(place, sseValue);
        }

        placesCleared = false;


        if (!sseMap.isEmpty()) {
            Map.Entry<String, Double> minEntry = CalculationHelper.minMapValue(sseMap);
            if (minEntry != null) {
                String foundPlaceId = minEntry.getKey();
                String outputTextview = "Der Ort ist: " + foundPlaceId + " mit Wert: " + minEntry.getValue() + "\n";

                /*Toast.makeText(context, "Ort: " + foundPlaceId, Toast.LENGTH_SHORT).show();*/
                if (!priorPlaceId.equals(foundPlaceId)) {
                    if ((priorPlaceId.equals("2") && foundPlaceId.equals("4")) ||
                            (priorPlaceId.equals("4") && foundPlaceId.equals("2")) ||
                            (priorPlaceId.equals("3") && foundPlaceId.equals("5")) ||
                            (priorPlaceId.equals("5") && foundPlaceId.equals("4"))) {
                        Log.d(TAG, "PlaceId changed but description stays the same");
                    } else {
                        notifyLocationChange(priorPlaceId, foundPlaceId);
                        updateLocalizationProgressUI(foundPlaceId, getLocationDescription(foundPlaceId));
                    }
                }
                priorPlaceId = foundPlaceId;

                /*Mit steigendem Sicherheitswert ist der sse klein für die Lokation und groß für
                die anderen Orte
                 */
                double sicherheitSse = CalculationHelper.sicherheitsWert(minEntry.getValue(), sseMap);
                String sicherheitString = "Der Sicherheitswert ist: " + sicherheitSse + "\n";

                StringBuilder sbSse = new StringBuilder();
                for (String key : sseMap.keySet()) {
                    sbSse.append(key).append(": ");
                    double sseValue = Math.round(sseMap.get(key) * 1000);
                    sseValue = sseValue / 1000;
                    sbSse.append(sseValue).append("\n");
                }
                String textViewAveragesString = outputTextview + sicherheitString + sbSse.toString();
                outputDetailedPlaceInfoDebug(textViewAveragesString);
            } else {
                Toast.makeText(getActivity(), "Durchschnittswerte fehlen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "sseMap empty");
        }

        wlanMeasure.clear();
    }

    protected void updateLocalizationProgressUI(String foundPlaceId, String locationDescription){
        progress.setTitle("Ort: " + foundPlaceId);
        progress.setMessage(locationDescription);
    }

    protected String getLocationDescription(String foundPlaceId) {
        String sDescription = "";
        switch (foundPlaceId) {
            case "1":
                sDescription = "Verlasse das Zimmer und gehe nach links";
                break;
            case "2":
                sDescription = "Gehe durch die Feuerschutztür und dann weiter den Gang runter";
                break;
            case "3":
                sDescription = "Gehe gerade aus durch den Notausgang links neben dem Vortragsraum";
                break;
            case "4":
                sDescription = "Gehe durch die Feuerschutztür und dann weiter den Gang runter";
                break;
            case "5":
                sDescription = "Gehe gerade aus durch den Notausgang links neben dem Vortragsraum";
                break;
        }
        return sDescription;
    }

    protected abstract void notifyLocationChange(String priorPlaceId, String foundPlaceId);


    protected void outputDetailedPlaceInfoDebug(String output){
        Log.i(TAG, output);
    }

    public  void bleAccess(Integer[] rssi){
        bleRssi[0] = rssi[0];
        bleRssi[1] = rssi[1];
        bleRssi[2] = rssi[2];
    }


}
