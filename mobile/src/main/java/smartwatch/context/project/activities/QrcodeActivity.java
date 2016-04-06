package smartwatch.context.project.activities;

import android.app.Activity;
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
import java.util.Queue;

import smartwatch.context.common.helper.BleHelper;
import smartwatch.context.project.R;

public class QrcodeActivity extends Activity implements BeaconConsumer, View.OnClickListener {
    private static final String TAG = "RangingActivity";
    private final Queue<Integer> rssiQueueBlue = new LinkedList<>();
    private final Queue<Integer> rssiQueueYellow = new LinkedList<>();
    private final Queue<Integer> rssiQueueRed = new LinkedList<>();
    private final int queueSize = 5;
    private final List<Integer> calibrationList = new LinkedList<>();
    /*Blue, Yelloow. Red*/
    private final double[] distances = new double[3];
    private final String uuidYellow = "FB:39:E6:2D:82:EF";
    private final String uuidBlue = "CE:BA:BE:97:DB:0C";
    private final String uuidRed = "DD:3F:50:F2:76:74";
    //*^+ oder
    private final double constMult = 0.0001060777;
    private final double constPower = 17.4228892910;
    private final double constPlus = 0.7610257596;
    private BeaconManager beaconManager;
    private int txPowerBlue;
    private int txPowerYellow;
    private int txPowerRed;
    private TextView distanceOutput;
    private TextView calibrationOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        Button calculateAverage = (Button) findViewById(R.id.calculate_average);
        calculateAverage.setOnClickListener(this);

        Button deleteList = (Button) findViewById(R.id.delete_list);
        deleteList.setOnClickListener(this);

        calibrationOutput = (TextView) findViewById(R.id.calibration_average);
        distanceOutput = (TextView) findViewById(R.id.ble_rssi);


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        /*Beacon.setDistanceCalculator(curveDistanceCalculator);*/
        beaconManager.bind(this);

    }

    public void onClick(View view) {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {


                if (beacons.size() > 0) {
                    Log.i(TAG, "<---------------------------------------------->");
                    Log.i(TAG, "beacons>0");
                    /*Create queues containing the latest 20 values*/
                    /*while (beacons.iterator().hasNext()) {*/
                    switch (beacons.iterator().next().getBluetoothAddress()) {
                        case uuidBlue:
                            if (rssiQueueBlue.size() < queueSize) {
                                rssiQueueBlue.add(beacons.iterator().next().getRssi());
                                Log.i(TAG, "BLAU signal1" + beacons.iterator().next().getRssi());
                            } else if (rssiQueueBlue.size() >= queueSize) {
                                rssiQueueBlue.remove();
                                rssiQueueBlue.add(beacons.iterator().next().getRssi());
                                Log.i(TAG, "BLAU signal2" + beacons.iterator().next().getRssi());
                            }
                            if (calibrationList.size() < 25) {
                                calibrationList.add(beacons.iterator().next().getRssi());
                            }

                            txPowerBlue = beacons.iterator().next().getTxPower();
                            Log.i(TAG, "Tx Power ist" + txPowerBlue);
                            Log.i(TAG, "Queue size ist" + rssiQueueBlue.size());

                            break;

                        case uuidYellow:
                            if (rssiQueueYellow.size() < queueSize) {
                                rssiQueueYellow.add(beacons.iterator().next().getRssi());
                            } else if (rssiQueueYellow.size() >= queueSize) {
                                rssiQueueYellow.remove();
                                rssiQueueYellow.add(beacons.iterator().next().getRssi());
                            }
                            Log.i(TAG, "Gelb signal" + beacons.iterator().next().getRssi());
                            txPowerYellow = beacons.iterator().next().getTxPower();
                            break;

                        case uuidRed:
                            if (rssiQueueRed.size() < queueSize) {
                                rssiQueueRed.add(beacons.iterator().next().getRssi());
                            } else if (rssiQueueRed.size() >= queueSize) {
                                rssiQueueRed.remove();
                                rssiQueueRed.add(beacons.iterator().next().getRssi());
                            }
                            Log.i(TAG, "Rot signal" + beacons.iterator().next().getRssi());
                            txPowerRed = beacons.iterator().next().getTxPower();
                            break;
                    }
                    /*}*/

                    /*Calculate Average for each List of RSSI Values*/
                    double avgRssiBlue = BleHelper.calculateAverage(rssiQueueBlue);
                    Log.i(TAG, "AvgRssi blau ist" + avgRssiBlue);
                    double avgRssiYellow = BleHelper.calculateAverage(rssiQueueYellow);
                    Log.i(TAG, "AvgRssi gelb ist" + avgRssiYellow);
                    double avgRssiRed = BleHelper.calculateAverage(rssiQueueRed);
                    Log.i(TAG, "AvgRssi rot ist" + avgRssiRed);

                    /*Calculate Distance based on received RSSI*/
                    CurveFittedDistanceCalculator curveDistanceCalculator =
                            new CurveFittedDistanceCalculator(constMult, constPower, constPlus);

                    distances[0] =
                            curveDistanceCalculator.calculateDistance(txPowerBlue, avgRssiBlue);
                    distances[1] =
                            curveDistanceCalculator.calculateDistance(txPowerYellow, avgRssiYellow);
                    distances[2] =
                            curveDistanceCalculator.calculateDistance(txPowerRed, avgRssiRed);

                    Log.i(TAG, distances[0] + " Distanz Blau");
                    Log.i(TAG, distances[1] + " Distanz Gelb");
                    Log.i(TAG, distances[2] + " Distanz Rot");
                    Log.i(TAG, "Größe Kalibrierungsliste: " + calibrationList.size());
                }

                /*Necessary to change UI data when no in the main thread*/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        distanceOutput.setText("Blau: " + distances[0] + "\n" +
                                "Gelb: " + distances[1] + "\n" +
                                "Rot: " + distances[2] + "\n" +
                                "Kalibrierungsliste: " + calibrationList.size());
                    }
                });
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

}
