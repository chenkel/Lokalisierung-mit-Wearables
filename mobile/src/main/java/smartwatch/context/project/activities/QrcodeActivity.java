package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import smartwatch.context.common.helper.BluetoothData;
import smartwatch.context.project.R;

public class QrcodeActivity extends Activity {
    private static final String TAG = "RangingActivity";

    /*Blue, Yelloow. Red*/

    private TextView distanceOutput;
    private TextView calibrationOutput;
    private ServiceConnection mConnection;
    boolean mBound = false;
    private BluetoothData bldata;

    public QrcodeActivity() {
        Log.d(TAG, "Constructor QrcodeActivity");
        mConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                BluetoothData.LocalBinder binder = (BluetoothData.LocalBinder) service;
                bldata = binder.getService();
                mBound = true;
                Toast.makeText(QrcodeActivity.this, "Connected", Toast.LENGTH_SHORT)
                        .show();
            }

            public void onServiceDisconnected(ComponentName className) {
                mBound = false;
            }
        };
    }

    /*BluetoothData bldata;*/
    /*Map<String, Number> avgRssi;*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "startService -- QRCodeActivty");
        this.bindService(new Intent(this, BluetoothData.class), mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        unbindService(mConnection);
        bldata.unbindManager();

        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        calibrationOutput = (TextView) findViewById(R.id.calibration_average);
        distanceOutput = (TextView) findViewById(R.id.ble_rssi);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*bldata.unbindManager(this);*/

    }

    /*public void onClick(View view) {
        if (view.getId() == R.id.calculate_average) {
            if (calibrationList.size() >= 20) {
                double calibrationAverage = BleHelper.calculateAverage(calibrationList);
                calibrationOutput.setText("AvgRssi: " + calibrationAverage + "\n" +
                        "Value: " + calibrationAverage / txPowerBlue + "\n" +
                        "TxPwr: " + txPowerBlue);
            } else {
                calibrationOutput.setText("Not enough values collected: " + calibrationList.size());
            }
        }

        if (view.getId() == R.id.delete_list) {
            calibrationList.clear();
        }
    }*/


}
