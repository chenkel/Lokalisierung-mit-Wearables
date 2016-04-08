package smartwatch.context.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import smartwatch.context.common.db.DatabaseHelper;

public abstract class CommonClass {
    private static final String TAG = CommonClass.class.getSimpleName();
    private Activity activity;

    protected ProgressDialog progress;
    protected WifiManager wifiManager;

    /* handler for received Intents for the "SCAN_RESULTS_AVAILABLE_ACTION" event */
    protected BroadcastReceiver resultReceiver;

    public DatabaseHelper db;
    public List<String> outputList;

    public CommonClass(Activity activity) {
        this.activity = activity;
        db = DatabaseHelper.getInstance(activity);
        wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(activity.getApplicationContext(), "WLAN wird eingeschaltet...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
        progress = new ProgressDialog(activity);

        outputList = new ArrayList<>();
    }


    protected void updateProgressOutput(int iProgress){
        if (progress != null) {
            progress.setProgress(iProgress);
        }
    }

    protected void hideProgressOutput(){
        if (progress != null) {
            progress.hide();
        }
    }

    protected void setMaxProgressOutput(int count) {
        if (progress != null) {
            progress.setMax(count);
        }
    }


    public void stopScanningAndCloseProgressDialog() {
        try {
            /* Stop the continous scan */
            activity.unregisterReceiver(resultReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, e.toString());
        } finally {
            /* Hide the loading spinner */
            hideProgressOutput();
        }


    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setResultReceiver(BroadcastReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }
}
