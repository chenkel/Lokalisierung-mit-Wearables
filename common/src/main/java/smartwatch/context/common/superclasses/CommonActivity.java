package smartwatch.context.common.superclasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Map;

import smartwatch.context.common.db.WlanAveragesDBAccess;
import smartwatch.context.common.db.WlanMeasurementsDBAccess;
import smartwatch.context.common.helper.CalculationHelper;
import smartwatch.context.common.helper.WlanMeasurements;

public class CommonActivity extends Activity {
    private static final String TAG = "CommonActivity";
    private Context context;
    protected final List<String> outputList = new ArrayList<>();
    /*OrientationHelper mOrientationHelper = null;*/
    protected final List<WlanMeasurements> wlanMeasure = new ArrayList<>();
    /* DBs*/
    protected WlanMeasurementsDBAccess measurementDB;
    protected String placeIdString;
    protected TextView textViewAverages;
    /* Measurement variables */
    private WifiManager wifiManager;
    private int scanCount = 0;
    protected int scanCountMax = 5;
    private boolean scanAndSave = true;
    private WlanAveragesDBAccess averagesDB;
    /* Initialize loading spinner */
    private ProgressDialog progress;

    private String priorPlaceId = "";
    private String foundPlaceId;
    private Vibrator v;
    /* handler for received Intents for the "SCAN_RESULTS_AVAILABLE_ACTION" event */
    private final BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "scanResultReceiver onReceive");
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
                Log.i(TAG, "wlanMeasure size: " + wlanMeasure.size());
                if (!scanAndSave) {
                    locateUser();
                    wifiManager.startScan();
                } else {
                    scanCount++;
                    progress.setProgress(scanCount);

                    if (scanCount < scanCountMax) {
                        wifiManager.startScan();
                    /* All scans finished */
                    } else {
                        outputDebugInfos();
                        stopScanningAndCloseProgressDialog();
                        saveMeasurements();
                    }
                }
            } else {
                stopScanningAndCloseProgressDialog();
                Toast.makeText(context, "Keine APs in der Umgebung gefunden", Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void stopScanningAndCloseProgressDialog() {
        try {
            /* Stop the continous scan */
            unregisterReceiver(scanResultReceiver);
        } catch (IllegalArgumentException e){
            Log.e(TAG, e.toString());
        }

        /* Hide the loading spinner */
        if (progress != null){
            progress.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        /* Unregister since the activity is not visible*/
        super.onPause();
        /*unregisterReceiver(scanResultReceiver);*/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = CommonActivity.this;

        progress = new ProgressDialog(this);

        /*Initialize Data Collection for Orientation*/
        /*mOrientationHelper = new OrientationHelper(this);*/

        /* Init DB */
        measurementDB = new WlanMeasurementsDBAccess(this);
        averagesDB = new WlanAveragesDBAccess(this);

        /* Enable Wi-Fi, if necessary */
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "WLAN wird eingeschaltet...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }



        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    protected void scanWlan() {
        if (placeIdString == null || placeIdString.isEmpty()) {
            Toast.makeText(context, "Bitte geben Sie eine ID für den aktuellen Ort an", Toast.LENGTH_SHORT).show();
            return;
        }
        outputList.clear();
        wlanMeasure.clear();

        scanCount = 0;

        scanAndSave = true;
        /* Register Listener to collect results */
        registerReceiver(scanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        /* show loading spinner */
        /*if (progress == null){
            progress = new ProgressDialog(this);
        }*/
        progress = new ProgressDialog(CommonActivity.this);

        progress.setTitle("Scan der WLAN-Umgebung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    protected void startLocalization() {
        outputList.clear();
        wlanMeasure.clear();

        scanAndSave = false;
        /* Register Listener to collect results */
        registerReceiver(scanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();


        progress = new ProgressDialog(CommonActivity.this);

        progress.setTitle("Lokalisierung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
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

    /*Activated when scanAndSave is true*/
    private void saveMeasurements() {
        new SaveScansTask().execute();
    }

    private void locateUser() {

        Log.i(TAG, "Now in locate user");

        if (wlanMeasure == null || wlanMeasure.size() == 0) {
            Toast.makeText(context, "Bitte führen sie eine Messung durch", Toast.LENGTH_SHORT).show();
            stopScanningAndCloseProgressDialog();
        }

        //if(receivedBluetooth)
        //Hashmap mit Key Bluetooth Cell und Value Array aus Places    ("BeaconBlau"| [1130, 1132, 1134, 1336])
        //placeList = [1130, 1132, 1134, 1336]

        //else
        //placeList = measurementDB.getAllPlaces();


                    /*Saves all places to placeList*/
        Cursor placeCursor = measurementDB.getAllPlaces();
        if (placeCursor.getCount() == 0) {
            Toast.makeText(context, "Keine Messdaten vorhanden", Toast.LENGTH_SHORT).show();
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
            Cursor modelCursor = averagesDB.getRssiByPlace(place);

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
                foundPlaceId = minEntry.getKey();
                String outputTextview = "Der Ort ist: " + foundPlaceId + " mit Wert: " + minEntry.getValue() + "\n";

                progress.setTitle("Ort: " + foundPlaceId);
                progress.setMessage("Gehen Sie zur Tür raus und dann nach links");

                /*Toast.makeText(context, "Ort: " + foundPlaceId, Toast.LENGTH_SHORT).show();*/
                if (!priorPlaceId.equals(foundPlaceId)){
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
                wlanMeasure.clear();
                /*Log.i(TAG, textViewAveragesString);*/
            } else {
                Toast.makeText(context, "Durchschnittswerte fehlen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "sseMap empty");
        }
    }

    protected void notifyLocationChange() {
        // Vibrate for 500 milliseconds if place changed
        v.vibrate(500);
    }

    protected void outputDetailedPlaceInfoDebug(String output) {}

    protected void deleteAllMeasurements() {
        measurementDB.deleteAllMeasurements();
        updateMeasurementsCount();
        Toast.makeText(context, "Alles Messungen wurden gelöscht", Toast.LENGTH_SHORT).show();
    }

    protected void deleteAllMeasurementsForPlace() {
        if (placeIdString == null || placeIdString.isEmpty()) {
            Toast.makeText(context, "Bitte geben Sie eine Ort-ID an.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CommonActivity.this);
        builder.setTitle("Messungen löschen...");
        builder.setMessage("Wirklich alle Messungen zu Ort-ID " + placeIdString + " löschen?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                measurementDB.deleteMeasurementForPlaceId(placeIdString);
                updateMeasurementsCount();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();


    }

    /* Stubs to be overridden by subclass*/
    protected void updateMeasurementsCount() {
    }

    protected void outputDebugInfos() {
    }

    private class SaveScansTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            progress.setTitle("Alle Scandaten werden gespeichert");
            progress.setMessage("Bitte warten Sie einen Moment...");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setMax(wlanMeasure.size());
            progress.show();
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            int scansCount = 0;
            try {
                for (WlanMeasurements ap : wlanMeasure) {
                    /* create a new record in DB */
                    measurementDB.createRecords(ap.getBssi(), ap.getSsid(), ap.getRssi(), placeIdString);
                    scansCount = scansCount + 1;
                    publishProgress(scansCount);
                }
                /*Toast.makeText(context, "Alle Messungen wurden mit der Orientierung gespeichert", Toast.LENGTH_SHORT).show();*/
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                /*Toast.makeText(context, "Die Messung konnte nicht gespeichert werden", Toast.LENGTH_SHORT).show();*/
            }
            return scansCount;
        }

        protected void onProgressUpdate(Integer... calcProgress) {
            progress.setProgress(calcProgress[0]);
        }

        protected void onPostExecute(Integer result) {
            progress.hide();
            updateMeasurementsCount();
        }
    }

    public class DoCalculationTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            progress.setTitle("Durchschnittliche Signalstärke aller APs für verschiede Orte wird berechnet");
            progress.setMessage("Bitte warten Sie einen Moment...");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.show();
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            int calculationsCount = 0;
            try {

                /* Clear previous averages */
                averagesDB.deleteMeasurements();

                /* Create list that contains average of all BSSIs for all places */
                Cursor bssiCursor = measurementDB.getRssiAvgByBssi();
                progress.setMax(bssiCursor.getCount());

                /*Cursor has placeId, bssi, ssid, avgrssi*/
                for (bssiCursor.moveToFirst(); !bssiCursor.isAfterLast(); bssiCursor.moveToNext()) {

                    /* Escape early if cancel() is called */
                    if (isCancelled()) break;
                    averagesDB.createRecords(bssiCursor.getString(0), bssiCursor.getString(1),
                            bssiCursor.getString(2), bssiCursor.getDouble(3));
                    calculationsCount = calculationsCount + 1;
                    publishProgress(calculationsCount);
                }

                bssiCursor.close();

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return calculationsCount;
        }

        protected void onProgressUpdate(Integer... calcProgress) {
            progress.setProgress(calcProgress[0]);
        }

        protected void onPostExecute(Integer result) {
            progress.hide();
        }

    }


}
