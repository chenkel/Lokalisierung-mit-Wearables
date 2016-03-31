package smartwatch.context.project.activities;

import android.os.Bundle;
import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import smartwatch.context.project.R;
import smartwatch.context.common.helper.BleHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.distance.CurveFittedDistanceCalculator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class QrcodeActivity extends Activity implements BeaconConsumer{
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    private double rssi;
    private Queue<Integer> rssiQueueBlue = new LinkedList<Integer>();
    private Queue<Integer> rssiQueueYellow= new LinkedList<Integer>();
    private Queue<Integer> rssiQueueRed = new LinkedList<Integer>();
    private int queueSize = 10;

    /*Blue, Yelloow. Red*/
    private double[] distances = new double[3];

    private final String uuidYellow = "FB:39:E6:2D:82:EF";
    private final String uuidBlue = "CE:BA:BE:97:DB:0C";
    private final String uuidRed = "DD:3F:50:F2:76:74";

    private int txPowerBlue;
    private int txPowerYellow;
    private int txPowerRed;

    private final double const1 = 0.9401940951;
    private final double const2 = 7;
    private final double const3 = 0.0;

    TextView distanceOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        distanceOutput = (TextView) findViewById(R.id.ble_rssi);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        /*Beacon.setDistanceCalculator(curveDistanceCalculator);*/
        beaconManager.bind(this);

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

                /* if (rssiQueue.size() < queueSize) {

                        Log.i(TAG, "+Does match, Address is " +
                                beacons.iterator().next().getBluetoothAddress());
                    } else if (rssiQueue.size() >= queueSize) {
                        rssiQueue.remove();
                        rssiQueue.add(beacons.iterator().next().getRssi());
                        Log.i(TAG, "+Does match, Address is " +
                                beacons.iterator().next().getBluetoothAddress());
                    }
                } else {
                    Log.i(TAG, "!!!Does not match, because Address is " +
                            beacons.iterator().next().getBluetoothAddress());


                    double avgSum = 0;
                    for (Integer element : rssiQueue) {
                        avgSum += element;
                    }
                    avgSum = avgSum / rssiQueue.size();
                    Log.i(TAG, "AvgSum ist " + avgSum);*/
                    /*Log.i(TAG, "Avg Sum " + avgSum + "\n" +
                            "Einzelwert Rssi " + beacons.iterator().next().getRssi() + "\n" +
                            "Erster Wert in der Queue " + rssiQueue.peek() + "\n" +
                            "Größe der Queue "+rssiQueue.size());*/


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


                    double avgRssiBlue = BleHelper.calculateAverage(rssiQueueBlue);
                    Log.i(TAG,"AvgRssi blau ist" + avgRssiBlue);
                    double avgRssiYellow = BleHelper.calculateAverage(rssiQueueYellow);
                    Log.i(TAG,"AvgRssi gelb ist" + avgRssiYellow);
                    double avgRssiRed = BleHelper.calculateAverage(rssiQueueRed);
                    Log.i(TAG,"AvgRssi rot ist" + avgRssiRed);

                    /*Berechnung aus Queues*/
                    CurveFittedDistanceCalculator curveDistanceCalculator =
                            new CurveFittedDistanceCalculator(const1, const2, const3);

                    distances[0] =
                            curveDistanceCalculator.calculateDistance(txPowerBlue,avgRssiBlue);
                    distances[1] =
                            curveDistanceCalculator.calculateDistance(txPowerYellow, avgRssiYellow);
                    distances[2] =
                            curveDistanceCalculator.calculateDistance(txPowerRed, avgRssiRed);

                    Log.i(TAG, distances[0] + " Distanz Blau");
                    Log.i(TAG, distances[1]+" Distanz Gelb");
                    Log.i(TAG, distances[2]+" Distanz Rot");
                }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distanceOutput.setText("Blau: " + distances[0]+"\n"+
                                                "Gelb: "+distances[1]+"\n"+
                                                "Rot: "+distances[2]);
                        }
                    });
            /*Log.i(TAG, "Die Distanz zu den Beacons ist 1,2,3");*/
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

}
