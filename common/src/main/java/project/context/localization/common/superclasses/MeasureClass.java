package project.context.localization.common.superclasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import project.context.localization.common.helper.WiFiMeasurement;


/**
 * The Measure class offers a convenient method {@link #measureWiFi()} to start
 * measuring a user's WiFi surrounding and define Fingerprints by {@link #setPlaceString(String)}
 * After a predefined scan count {@link #setScanCountMax(int)}, the measurements
 * are saved in the database. These entries are then used later
 * in {@link AverageMeasuresClass} and {@link LocalizationClass} to aggregate
 * WiFi-AP RSSis and to locate the user.
 */
public abstract class MeasureClass extends CommonClass {
    private static final String TAG = MeasureClass.class.getSimpleName();
    /**
     * The property defines the maximum count of scans.
     */
    public int scanCountMax;
    /**
     * The Wlan measure list holds all scan results for the WiFI-Access point environment.
     */
    protected final List<WiFiMeasurement> wiFiMeasurements = new ArrayList<>();
    /**
     * The Place id string is used to assign the measurement to a place.
     */
    protected String placeString;
    /**
     * Scan count keeps track of the current scan iteration.
     */
    private int scanCount;

    /**
     * Instantiates a new Measure class.
     *
     * @param activity a reference to the instantiating activity to access its UI elements
     */
    public MeasureClass(Activity activity) {
        super(activity);

        scanCountMax = 3;
        scanCount = 0;
    }

    /**
     * Sets the max scan count.
     *
     * @param sMax the s max
     */
    public void setScanCountMax(int sMax) {
        this.scanCountMax = sMax;
    }

    /**
     * Sets place id string.
     *
     * @param placeString the place string
     */
    public void setPlaceString(String placeString) {
        this.placeString = placeString;
    }

    /**
     * Stops measuring.
     * <p>
     * Therefore it unregisters the result receiver.
     * In the end it hides the progress dialog/output.
     */
    public void stopMeasuring() {
        try {
            /* Stop the continous scan */
            getActivity().unregisterReceiver(measureResultReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, e.toString());
        } finally {
            /* Hide the loading spinner */
            hideProgressOutput();
        }
    }

    /**
     * Initiates the measuring process.
     * <p>
     * This method gets called by the activities.
     * In a first step, a result receiver for the WiFi results gets registered.
     * Then, the WiFi scanning starts and a progress is shown to the user.
     */
    public void measureWiFi() {
        if (placeString == null || placeString.isEmpty()) {
            Log.e(TAG, "placeString empty");
            return;
        }

        wiFiMeasurements.clear();

        scanCount = 0;

        /* Register Listener to collect results */
        getActivity().registerReceiver(measureResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        showMeasureProgress();
    }


    /**
     * Show measure progress in progress output.
     * <p>
     * Should be overridden by devices like Moto 360
     * to be substituted by other UI elements, that fit the plattform best.
     */
    protected void showMeasureProgress() {
        progress = new ProgressDialog(getActivity());

        progress.setTitle("Messung der Signalstärke der WiFi-APs...");
        progress.setMessage("");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    /**
     * Output debug infos.
     *
     * @param wiFiMeasurementList the list containing measurement results
     *                            of the WiFi access points in the current area.
     */
    protected void outputDebugInfos(List<WiFiMeasurement> wiFiMeasurementList) {
        Log.d(TAG, wiFiMeasurementList.toString());
    }

    /**
     * Show save progress.
     */
    protected void showMeasuresSaveProgress() {
        progress.setTitle("Alle Messdaten werden gespeichert...");
        progress.setMessage("");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(wiFiMeasurements.size());
        progress.show();
    }

    /**
     * Delete all measurements.
     */
    public void deleteAllMeasurements() {
        db.deleteAllMeasurements();
        updateMeasurementsCount();
        Toast.makeText(getActivity(), "Alles Messungen wurden gelöscht", Toast.LENGTH_SHORT).show();
    }

    /**
     * Delete all measurements for a specific place.
     */
    public void deleteAllMeasurementsForPlace() {
        if (placeString == null || placeString.isEmpty()) {
            Toast.makeText(getActivity(), "Bitte geben Sie eine Ort-ID an.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Messungen löschen...");
        builder.setMessage("Wirklich alle Messungen zu Ort-ID " + placeString + " löschen?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                db.deleteMeasurementForPlaceId(placeString);
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

    /**
     * Update measurements count.
     * <p>
     * Normally gets called after new measurements are
     * saved in the db, so that changes can be queried after they are done.
     */
    public abstract void updateMeasurementsCount();


    /**
     * SaveMeasuresTask is an Async Task that saves all
     * the measurements in the db and displays the progress to the user.
     */
    private class SaveMeasuresTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            showMeasuresSaveProgress();
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            int scansCount = 0;
            try {
                for (WiFiMeasurement ap : wiFiMeasurements) {
                    /* create a new record in DB */
                    long affectedRow = db.addMeasurementsRecords(ap.getBssi(), ap.getSsid(), ap.getRssi(), placeString);
                    if (affectedRow == -1) {
                        Log.e(TAG, "Error inserting average Records");
                    }

                    scansCount = scansCount + 1;
                    publishProgress(scansCount);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return scansCount;
        }

        protected void onProgressUpdate(Integer... calcProgress) {
            updateProgressOutput(calcProgress[0]);
        }

        protected void onPostExecute(Integer result) {
            updateMeasurementsCount();
            stopMeasuring();
        }
    }

    /**
     * The Broadcast receiver receives all WiFi Scan results and
     * adds them to the wiFiMeasurements list.
     * The method also updates the progress output, returns
     * debugging information about the scanning.
     * In the end the WiFi Scanning stops and all
     * Measurements are saved in the DB asynchronously.
     */
    private final BroadcastReceiver measureResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> currentResults = wifiManager.getScanResults();

            int measurementCount = currentResults.size();
            if (measurementCount > 0) {
                for (ScanResult result : currentResults) {
                    wiFiMeasurements.add(new WiFiMeasurement(result.BSSID, result.level, result.SSID));
                }

                scanCount++;
                updateProgressOutput(scanCount);
                if (scanCount >= scanCountMax) {
                /* All scans finished */
                    outputDebugInfos(wiFiMeasurements);
                    stopMeasuring();
                    new SaveMeasuresTask().execute();
                } else {
                    /* Still some scans to perform */
                    wifiManager.startScan();
                }

            } else {
                stopMeasuring();
                Toast.makeText(context, "Keine APs in der Umgebung gefunden", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
