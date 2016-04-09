package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.distance.CurveFittedDistanceCalculator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import smartwatch.context.common.helper.BleHelper;
import smartwatch.context.common.helper.BluetoothData;
import smartwatch.context.common.helper.BluetoothMeasurements;
import smartwatch.context.project.R;

public class QrcodeActivity extends Activity{
    private static final String TAG = "RangingActivity";

    /*Blue, Yelloow. Red*/

    private TextView distanceOutput;
    private TextView calibrationOutput;
    BluetoothData bldata;

    /*BluetoothData bldata;*/
    /*Map<String, Number> avgRssi;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        calibrationOutput = (TextView) findViewById(R.id.calibration_average);
        distanceOutput = (TextView) findViewById(R.id.ble_rssi);

        startService(new Intent(this, BluetoothData.class));
        /*bldata = new BluetoothData(this);*/
        /*distanceOutput.setText(bldata.getRssiOutput());*/
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
