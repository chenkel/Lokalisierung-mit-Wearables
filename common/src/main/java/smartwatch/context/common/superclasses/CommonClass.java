package smartwatch.context.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import smartwatch.context.common.db.DatabaseHelper;

public abstract class CommonClass {
    private static final String TAG = CommonClass.class.getSimpleName();
    protected Activity activity;

    ProgressDialog progress;
    final WifiManager wifiManager;

    protected final DatabaseHelper db;
    public final List<String> outputList;

    CommonClass(Activity activity) {
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
            Log.w(TAG, "progress is not null");
            progress.dismiss();
        }
    }

    protected void setMaxProgressOutput(int count) {
        if (progress != null) {
            progress.setMax(count);
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
