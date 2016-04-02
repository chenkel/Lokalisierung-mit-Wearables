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
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartwatch.context.common.db.WlanAveragesDBAccess;
import smartwatch.context.common.db.WlanMeasurementsDBAccess;
import smartwatch.context.common.helper.CalculationHelper;
import smartwatch.context.common.helper.OrientationHelper;
import smartwatch.context.common.helper.WlanMeasurements;

public class CommonActivity extends Activity {
    private static final String TAG = "CommonActivity";

    /* Measurement variables */
    protected WifiManager wifiManager;
    protected int measurementCount;

    protected List<String> outputList = new ArrayList<>();
    protected int scanCount = 0;
    protected int scanCountMax = 10;
    protected int scanCountLocalization = 1;
    protected int scanCountScanning = 3;
    protected boolean scanAndSave = true;
    OrientationHelper mOrientationHelper = null;
    protected List<WlanMeasurements> wlanMeasure = new ArrayList<>();


    /* DBs*/
    protected WlanMeasurementsDBAccess measurementDB;
    protected WlanAveragesDBAccess averagesDB;

    protected String placeIdString;

    protected TextView textViewAverages;
    protected ProgressDialog progress;
    protected Toast toast;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Customize toast*/
        toast = Toast.makeText(CommonActivity.this, "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);

        /*Initialize Data Collection for Orientation*/
        mOrientationHelper = new OrientationHelper(this);

        /* Init DB */
        measurementDB = new WlanMeasurementsDBAccess(this);
        averagesDB = new WlanAveragesDBAccess(this);

        /* Enable Wi-Fi, if necessary */
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            toast.setText("WLAN wird eingeschaltet...");
            toast.show();
            wifiManager.setWifiEnabled(true);
        }

        /* Initialize loading spinner */
        progress = new ProgressDialog(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientationHelper.register();
    }

    @Override
    protected void onPause() {
        /* Unregister since the activity is not visible */
        super.onPause();
        mOrientationHelper.unregister();
    }

    /* handler for received Intents for the "SCAN_RESULTS_AVAILABLE_ACTION" event */
    protected BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Orientation aus der Wlan Activity " + OrientationHelper.orientationValue);
            List<ScanResult> currentResults = wifiManager.getScanResults();

            measurementCount = currentResults.size();
            if (measurementCount > 0) {
                scanCount++;
                progress.setProgress(scanCount);

                for (ScanResult result : currentResults) {
                    wlanMeasure.add(new WlanMeasurements(
                            result.BSSID,
                            result.level,
                            result.SSID,
                            OrientationHelper.orientationValue
                    ));
                }

                if (scanCount < scanCountMax) {
                    wifiManager.startScan();
                    /* All scans finished */
                } else {
                    outputDebugInfos();

                    /* Stop the continous scan */
                    unregisterReceiver(scanResultReceiver);

                    /* Hide the loading spinner */
                    progress.dismiss();

                    if (scanAndSave) {
                        saveMeasurements();
                    } else {
                        locateUser();
                    }
                }
            } else {
                /* Stop the continous scan */
                unregisterReceiver(scanResultReceiver);
                /* Hide the loading spinner */
                progress.dismiss();
                toast.setText("Keine APs in der Umgebung gefunden");
                toast.show();
            }

        }
    };

    protected void scanWlan() {
        if (placeIdString == null || placeIdString.isEmpty()) {
            toast.setText("Bitte geben Sie eine ID für den aktuellen Ort an");
            toast.show();
            return;
        }
        outputList.clear();
        wlanMeasure.clear();


        scanCount = 0;
        scanCountMax = scanCountScanning;
        scanAndSave = true;
        /* Register Listener to collect results */
        registerReceiver(scanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        /* show loading spinner */
        progress.setTitle("Scan der WLAN-Umgebung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    protected void startLocalization() {
        scanAndSave = false;
        progress.setTitle("Lokalisierung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setMax(1);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();

        outputList.clear();
        wlanMeasure.clear();

        scanCountMax = scanCountLocalization;

        /* Register Listener to collect results */
        registerReceiver(scanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();
    }

    /*Activated when scanAndSave is true*/
    protected void saveMeasurements() {
        new SaveScansTask().execute();
    }

    public class SaveScansTask extends AsyncTask<Void, Integer, Integer> {
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
                toast.setText("Alle Messungen wurden mit der Orientierung gespeichert");
                toast.show();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                toast.setText("Die Messung konnte nicht gespeichert werden");
                toast.show();
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
                Log.i(TAG, e.toString());
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

    protected void locateUser() {
        Log.i(TAG, "Now in locate user");

        if (wlanMeasure == null || wlanMeasure.size() == 0) {
            toast.setText("Bitte führen sie eine Messung durch");
            toast.show();
            return;
        }
                    /*Saves all places to placeList*/
        Cursor placeCursor = measurementDB.getAllPlaces();
        if (placeCursor.getCount() == 0) {
            toast.setText("Keine Messdaten vorhanden");
            toast.show();
            return;
        }
        ArrayList<String> placeList = new ArrayList<>();
        for (placeCursor.moveToFirst(); !placeCursor.isAfterLast(); placeCursor.moveToNext()) {
            placeList.add(placeCursor.getString(0));
        }

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

            double sseValue = CalculationHelper.calculateSse(wlanMeasure, modellWerte);
            Log.i(TAG, " " + sseValue);
            sseMap.put(place, sseValue);
        }
        if (!sseMap.isEmpty()) {
            Map.Entry<String, Double> minEntry = CalculationHelper.minMapValue(sseMap);
            if (minEntry != null) {
                String foundPlaceId = minEntry.getKey();
                String outputTextview = "Der Ort ist: " + foundPlaceId + "\n";

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

                textViewAverages.setText(textViewAveragesString);
            } else {
                toast.setText("Durchschnittswerte fehlen");
                toast.show();
            }
        } else {
            Log.e(TAG, "sseMap empty");
        }
    }

    protected void deleteAllMeasurementsForPlace() {
        if (placeIdString == null || placeIdString.isEmpty()) {
            toast.setText("Bitte geben Sie eine Ort-ID an.");
            toast.show();
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
    protected void updateMeasurementsCount(){}
    protected void outputDebugInfos(){}


}
