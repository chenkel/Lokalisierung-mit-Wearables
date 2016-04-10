package smartwatch.context.common.helper;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class BluetoothData extends IntentService implements BeaconConsumer {

    private static final String TAG = "Bluetooth Data";

    private final IBinder mBinder = new LocalBinder();
    private final String[] bluePlaces = {"1", "2", "3"};

    //*^+ Konstanten zur Berechnung der Distanz
    /*private final double constMult = 0.0001060777;
    private final double constPower = 17.4228892910;
    private final double constPlus = 0.7610257596;*/
    private final String[] yellowPlaces = {"11", "12", "13"};
    private final String[] redPlaces = {"21", "22", "23"};
    /*Queue that holds the measured RSSI for each beacon*/
    private final Queue<Integer> rssiQueueBlue = new LinkedList<>();
    private final Queue<Integer> rssiQueueYellow = new LinkedList<>();
    private final Queue<Integer> rssiQueueRed = new LinkedList<>();
    /*Sets the size of each queue. Better averages vs shorter response to change*/
    private final int queueSize = 5;
    /*The UUID of each Beacon*/
    private final String uuidYellow = "FB:39:E6:2D:82:EF";
    private final String uuidBlue = "CE:BA:BE:97:DB:0C";
    private final String uuidRed = "DD:3F:50:F2:76:74";
    Map<String, Number> avgRssi;
    BluetoothData bldata;
    private BeaconManager beaconManager;
    /*Constant txPower transmitted by the beacons*/
    private int txPowerBlue;
    private int txPowerYellow;
    private int txPowerRed;
    /*Average RSSI of each queue*/
    private double avgBlue = 0;
    private double avgYellow = 0;
    private double avgRed = 0;


    public BluetoothData() {
        Log.i(TAG, "Im Konstruktor von BluetoothData");
    }

    public int getRssiQueueBlue() {
        return rssiQueueBlue.size();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate -- BluetoothData");
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {



                /*BluetoothMeasurements blueBeacon = new BluetoothMeasurements(
                        "blue",uuidBlue,bluePlaces);
                BluetoothMeasurements yellowBeacon = new BluetoothMeasurements(
                        "yellow",uuidYellow,yellowPlaces);
                BluetoothMeasurements redBeacon = new BluetoothMeasurements(
                        "red",uuidRed,redPlaces);*/

                if (beacons.size() > 0) {
                    Log.w(TAG, "Beacon size: " + beacons);
                    /*Create queues containing the latest 20 values*/
                    /*while (beacons.iterator().hasNext()) {*/

                    avgRssi =
                            queueAssignment(beacons.iterator().next().getBluetoothAddress(),
                                    beacons.iterator().next().getRssi());
                    Log.i(TAG, "Die Größe der Queue Blue ist" + getRssiQueueBlue());

                    Log.i(TAG, avgRssi.toString());
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public Map<String, Number> queueAssignment(String uuid, int rssi) {
        switch (uuid) {
            case uuidBlue:
                if (rssiQueueBlue.size() < queueSize) {
                    rssiQueueBlue.add(rssi);
                } else if (rssiQueueBlue.size() >= queueSize) {
                    rssiQueueBlue.remove();
                    rssiQueueBlue.add(rssi);
                }
                break;

            case uuidYellow:
                if (rssiQueueYellow.size() < queueSize) {
                    rssiQueueYellow.add(rssi);
                } else if (rssiQueueYellow.size() >= queueSize) {
                    rssiQueueYellow.remove();
                    rssiQueueYellow.add(rssi);
                }
                break;

            case uuidRed:
                if (rssiQueueRed.size() < queueSize) {
                    rssiQueueRed.add(rssi);
                } else if (rssiQueueRed.size() >= queueSize) {
                    rssiQueueRed.remove();
                    rssiQueueRed.add(rssi);
                }
                break;
        }

        avgBlue = calculateAverage(rssiQueueBlue);
        avgYellow = calculateAverage(rssiQueueYellow);
        avgRed = calculateAverage(rssiQueueRed);

        Map<String, Number> avgValues = new HashMap<>();
        avgValues.put("blue", avgBlue);
        avgValues.put("yellow", avgYellow);
        avgValues.put("red", avgRed);


        return avgValues;
    }

    public double calculateAverage(Collection<Integer> queue) {
        double avgSum = 0;
        for (Integer element : queue) {
            avgSum += element;
        }
        return avgSum / queue.size();
    }

//    public void unbindManager() {
////        Log.w(TAG, "unbindManager - BluetoothData");
////        beaconManager.unbind(this);
//
//    }


    public String getRssiOutput() {
        return "getRssiOutput Output";
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind -- BluetoothData");
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand -- BluetoothData");
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {
        public BluetoothData getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothData.this;
        }
    }


}
