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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartwatch.context.common.helper.CalculationHelper;
import smartwatch.context.common.helper.WlanMeasurements;

public abstract class Localization extends CommonClass {
    private static final String TAG = Localization.class.getSimpleName();
    private List<WlanMeasurements> wlanMeasure = new ArrayList<>();
    private String priorPlaceId = "";

    protected final BroadcastReceiver localizationScanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*Log.i(TAG, "localizationScanResultReceiver onReceive");*/
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
        if (!sseMap.isEmpty()) {
            Map.Entry<String, Double> minEntry = CalculationHelper.minMapValue(sseMap);
            if (minEntry != null) {
                String foundPlaceId = minEntry.getKey();
                String outputTextview = "Der Ort ist: " + foundPlaceId + " mit Wert: " + minEntry.getValue() + "\n";

                updateLocalizationProgressUI(foundPlaceId, getLocationDescription(foundPlaceId));

                /*Toast.makeText(context, "Ort: " + foundPlaceId, Toast.LENGTH_SHORT).show();*/
                if (!priorPlaceId.equals(foundPlaceId)) {
                    notifyLocationChange();
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
                sDescription = "Verlasse das Zimmer und gehe nach rechts";
                break;
            case "2":
                sDescription = "Geh weiter den Gang runter";
                break;
            case "3":
                sDescription = "Geh weiter durch die Tür";
                break;
            case "4":
                sDescription = "Geh zur Treppe und dann nach unten";
                break;
            case "5":
                sDescription = "Geh bis nach ganz unten";
                break;
        }
        return sDescription;
    }

    protected abstract void notifyLocationChange();


    protected void outputDetailedPlaceInfoDebug(String output){
        Log.i(TAG, output);
    }


}
