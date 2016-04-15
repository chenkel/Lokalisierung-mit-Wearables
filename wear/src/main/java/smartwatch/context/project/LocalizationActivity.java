package smartwatch.context.project;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import smartwatch.context.common.helper.BluetoothData;
import smartwatch.context.common.superclasses.Localization;

public class LocalizationActivity extends Activity {

    private static final String TAG = LocalizationActivity.class.getSimpleName();

    private ServiceConnection mConnection;
    boolean mBound = false;
    private BluetoothData bldata;

    private DelayedConfirmationView mDelayedView;
    private Localization mLocalization;
    private Vibrator v;


    public LocalizationActivity(){
        Log.w(TAG, "Constructor");
        mConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                BluetoothData.LocalBinder binder = (BluetoothData.LocalBinder) service;
                bldata = binder.getService();
                mBound = true;
                Toast.makeText(LocalizationActivity.this, "Connected", Toast.LENGTH_SHORT)
                        .show();
            }

            public void onServiceDisconnected(ComponentName className) {
                mBound = false;
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        final TextView descriptionTextView = (TextView) findViewById(R.id.description);

        mLocalization = new Localization(this) {

            @Override
            protected void notifyLocationChange(String priorPlaceId, String foundPlaceId) {
                v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
            }

            @Override
            protected void showLocalizationProgressOutput() {}

            @Override
            protected void updateLocalizationProgressUI(String foundPlaceId, String locationDescription) {
                descriptionTextView.setText(locationDescription);
            }
        };
        mLocalization.startLocalization();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "startService");

        this.bindService(new Intent(this, BluetoothData.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        mLocalization.stopScanningAndCloseProgressDialog();
        super.onPause();

    }
}