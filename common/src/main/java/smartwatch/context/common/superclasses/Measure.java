package smartwatch.context.common.superclasses;

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
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import smartwatch.context.common.helper.WlanMeasurements;


public abstract class Measure extends CommonClass {
    private static final String TAG = Measure.class.getSimpleName();

    public void setScanCountMax(int scanCountMax) {
        this.scanCountMax = scanCountMax;
    }

    public void setPlaceIdString(String placeIdString) {
        this.placeIdString = placeIdString;
    }

    private int scanCount;
    protected List<WlanMeasurements> wlanMeasure = new ArrayList<>();
    public int scanCountMax;

    protected String placeIdString;

    long tstamp = 0;
    long diff;
    List<Long> timeList = new ArrayList<Long>();
    List<Integer> rssiListMeasure = new ArrayList<Integer>();
    int rssiMeasure;
    long inittime = 0;

    protected final BroadcastReceiver measureResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(inittime == 0){inittime = System.currentTimeMillis()/1000;}
            List<ScanResult> currentResults = wifiManager.getScanResults();

            int measurementCount = currentResults.size();
            /*Log.i(TAG, "measurementCount: " + measurementCount);*/
            if (measurementCount > 0) {


                for (ScanResult result : currentResults) {

                    /*WLAN Anaalysis*/
                    /*if(result.BSSID.equals("4c:96:14:21:21:c4")){
                        tstamp = System.currentTimeMillis()/1000 - inittime;
                        rssiMeasure = Math.abs(result.level);
                        timeList.add(tstamp);
                        rssiListMeasure.add(rssiMeasure);
                        Log.i(TAG, rssiMeasure+"a");
                        Log.i(TAG, tstamp+"b");
                    }*/

/*                    Log.i(TAG, "###new Timestamp: " + tstamp);
                    Log.i(TAG, "Rssi: " + result.level);*/

                    wlanMeasure.add(new WlanMeasurements(
                            result.BSSID,
                            result.level,
                            result.SSID,
                            0
                    ));
                }
                /*Log.i(TAG, "wlanMeasure size: " + wlanMeasure.size());*/



                scanCount++;
                updateProgressOutput(scanCount);
                    /* All scans finished */
                if (scanCount >= scanCountMax) {

                    /*WLAN Logging*/
                    /*StringBuilder sbRssi = new StringBuilder();
                    StringBuilder sbTs = new StringBuilder();

                    for(int i : rssiListMeasure){
                        sbRssi.append(i+",");
                    }

                    for(long j : timeList){
                        sbRssi.append(j+",");
                    }

                    Log.i(TAG, sbRssi.toString()+"\n");
                    Log.i(TAG, sbTs.toString());*/
                    /*WLAN Logging*/


                    outputDebugInfos(wlanMeasure);
                    stopScanningAndCloseProgressDialog();
                    new SaveMeasuresTask().execute();
                } else {
                    wifiManager.startScan();
                }

            } else {
                stopScanningAndCloseProgressDialog();
                Toast.makeText(context, "Keine APs in der Umgebung gefunden", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public Measure(Activity activity) {
        super(activity);
        setResultReceiver(measureResultReceiver);

        scanCountMax = 3;
        scanCount = 0;
    }

    public void measureWlan() {
        if (placeIdString == null || placeIdString.isEmpty()) {
/*            Toast.makeText(Measure.this, "Bitte geben Sie eine ID für den aktuellen Ort an", Toast.LENGTH_SHORT).show();*/
            Log.e(TAG, "placeIdString empty");
            return;
        }
        outputList.clear();
        wlanMeasure.clear();

        scanCount = 0;

        /* Register Listener to collect results */
        getActivity().registerReceiver(measureResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        showMeasureProgress();
    }

    protected void showMeasureProgress() {
        progress = new ProgressDialog(getActivity());

        progress.setTitle("Messung der Signalstärken der WiFi-APs...");
//        progress.setMessage("Durchschnittliche Signalstärke aller APs für verschiede Orte wird berechnet");

//        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setMessage("");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    protected void outputDebugInfos(List<WlanMeasurements> wlanMeasure){
        Log.d(TAG, wlanMeasure.toString());
    }

    protected void showMeasuresSaveProgress() {
        progress.setTitle("Alle Messdaten werden gespeichert...");
//        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setMessage("");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(wlanMeasure.size());
        progress.show();
    }

    public void deleteAllMeasurements() {
        db.deleteAllMeasurements();
        updateMeasurementsCount();
        Toast.makeText(getActivity(), "Alles Messungen wurden gelöscht", Toast.LENGTH_SHORT).show();
    }

    public void deleteAllMeasurementsForPlace() {
        if (placeIdString == null || placeIdString.isEmpty()) {
            Toast.makeText(getActivity(), "Bitte geben Sie eine Ort-ID an.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Messungen löschen...");
        builder.setMessage("Wirklich alle Messungen zu Ort-ID " + placeIdString + " löschen?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                db.deleteMeasurementForPlaceId(placeIdString);
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

    public abstract void updateMeasurementsCount();

    protected class SaveMeasuresTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            showMeasuresSaveProgress();
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            int scansCount = 0;
            try {
                for (WlanMeasurements ap : wlanMeasure) {
                    /* create a new record in DB */
                    db.createMeasurementsRecords(ap.getBssi(), ap.getSsid(), ap.getRssi(), placeIdString);
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
            updateProgressOutput(calcProgress[0]);
        }

        protected void onPostExecute(Integer result) {
            updateMeasurementsCount();
            hideProgressOutput();
        }
    }
}
