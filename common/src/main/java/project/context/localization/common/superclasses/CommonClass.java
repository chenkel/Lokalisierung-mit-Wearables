package project.context.localization.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import project.context.localization.common.db.DatabaseHelper;

/**
 * The abstract class CommonClass unifies methods and attributes
 * used by {@link LocalizationClass}, {@link MeasureClass} and {@link AverageMeasuresClass}.
 * It thereby offers access to the database, progressDialogs and the activity
 * using the classes itself.
 */
public abstract class CommonClass {
    private static final String TAG = CommonClass.class.getSimpleName();
    /**
     * The Activity attribute holds a reference to the initiating activity.
     * With the help of this variable, ProgressDialogs can be shown in the activity's context.
     */
    protected Activity activity;

    /**
     * The ProgressDialog object to give the user visual feedback on progress.
     */
    protected ProgressDialog progress;
    /**
     * The Wifi manager object holding functionality to check WiFi Access Points
     * in the environment of the user.
     */
    protected final WifiManager wifiManager;

    /**
     * The Db instance managing all information for measurements and averages.
     */
    protected final DatabaseHelper db;


    /**
     * Instantiates a new Common class with the reference to an activity to influence UI elements.
     * First a db instance gets initiated.
     * After that the wifiManager and the progressDialog are getting initiated and prepared.
     *
     * @param activity the activity which initially instantiated the subclass of CommonClass
     */
    CommonClass(Activity activity) {
        this.activity = activity;

        db = DatabaseHelper.getInstance(activity);

        wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(activity.getApplicationContext(), "WLAN wird eingeschaltet...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
        progress = new ProgressDialog(activity);
    }


    /**
     * Update progress output.
     *
     * @param iProgress the int for the current progress
     */
    protected void updateProgressOutput(int iProgress){
        if (progress != null) {
            progress.setProgress(iProgress);
        }
    }

    /**
     * Hide progress output, if it exists.
     */
    protected void hideProgressOutput(){
        if (progress != null) {
            Log.w(TAG, "progress is not null");
            progress.dismiss();
        }
    }

    /**
     * Sets max progress output.
     *
     * @param maxProgress the maximum of the progress.
     */
    protected void setMaxProgressOutput(int maxProgress) {
        if (progress != null) {
            progress.setMax(maxProgress);
        }
    }

    /**
     * Gets activity.
     *
     * @return the activity
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * Sets activity.
     *
     * @param activity the activity
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
