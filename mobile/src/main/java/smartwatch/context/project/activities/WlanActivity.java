package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import smartwatch.context.common.helper.BluetoothData;
import smartwatch.context.common.helper.WlanMeasurements;
import smartwatch.context.common.superclasses.AverageMeasures;
import smartwatch.context.common.superclasses.Localization;
import smartwatch.context.common.superclasses.Measure;
import smartwatch.context.project.R;


public class WlanActivity extends Activity implements View.OnClickListener, BeaconConsumer {
    private static final String TAG = "WlanActivity";

    private Localization mLocalization;
    private AverageMeasures mAverage;
    private Measure mMeasure;
    private EditText editPlaceId;
    private TextView textViewDebug;
    private TextView textViewMeasuresCount;
    private ArrayAdapter wifiArrayAdapter;
    protected WifiManager wifiManager;

    private ServiceConnection mConnection;
    boolean mBound = false;
    private BluetoothData bldata;
    BeaconManager beaconManager;

    /*blue, red, yellow*/
    Integer[] rssiArray = {-200,-200,-200};
    double rssi;
    private final String uuidYellow = "FB:39:E6:2D:82:EF";
    private final String uuidBlue = "CE:BA:BE:97:DB:0C";
    private final String uuidRed = "DD:3F:50:F2:76:74";

    Beacon measuredBeacon;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlan);
        Log.v(TAG, "WlanActivity constructor called.");

        Context context = WlanActivity.this;

        /* Enable Wi-Fi, if necessary */
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "WLAN wird eingeschaltet...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        /* Register components from activity */
        final ListView wifiListView = (ListView) findViewById(R.id.listViewWifi);
        Button buttonScan = (Button) findViewById(R.id.wlan_scan);
        buttonScan.setOnClickListener(this);

        /* Initialize editPlaceId and its listener for the editText field */
        editPlaceId = (EditText) findViewById(R.id.place_id);
        editPlaceId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                mMeasure.updateMeasurementsCount();
            }
        });

        /* Initialize buttonCalculateAverages and its listener for the button */
        Button buttonCalculateAverages = (Button) findViewById(R.id.wlan_calculate_averages);
        buttonCalculateAverages.setOnClickListener(this);

        /* Initialize buttonCalculateAverages and its listener for the button */
        Button buttonLocalization = (Button) findViewById(R.id.wlan_localization);
        buttonLocalization.setOnClickListener(this);

        /* Initialize buttonDeleteAllMeasurements and its listener for the button */
        Button buttonDeleteAllMeasurements = (Button) findViewById(R.id.wlan_delete_measurements_for_place);
        buttonDeleteAllMeasurements.setOnClickListener(this);

        /* Initialize textViewDebug */
        textViewDebug = (TextView) findViewById(R.id.debug_text);
        textViewMeasuresCount = (TextView) findViewById(R.id.wlan_prev_scan_count);

        /* Setup ArrayAdapter displaying scan results */
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        /*Beacon.setDistanceCalculator(curveDistanceCalculator);*/
        beaconManager.bind(this);


        mLocalization = new Localization(this) {
            @Override
            protected void notifyLocationChange(String priorPlaceId, String foundPlaceId) {
                // Vibrate for 500 milliseconds if place changed
/*                v.vibrate(500);*/
            }

            @Override
            protected void outputDetailedPlaceInfoDebug(String output) {
                textViewDebug.setText(output);
            }
        };

        mMeasure = new Measure(this) {

            @Override
            protected void outputDebugInfos(List<WlanMeasurements> wlanMeasure) {
                /* Sorting of WlanMeasurements */
                Comparator<WlanMeasurements> wlanComparator = new Comparator<WlanMeasurements>() {
                    @Override
                    public int compare(WlanMeasurements lhs, WlanMeasurements rhs) {
                        return (lhs.getRssi() > rhs.getRssi() ? -1 : (lhs.getRssi() == rhs.getRssi() ? 0 : 1));
                    }
                };

                Collections.sort(wlanMeasure, wlanComparator);

                /* only show last measurement in list */
                for (WlanMeasurements ap : wlanMeasure) {
                    String helperString = "SSID: " + ap.getSsid()
                            + "\nRSSI: " + ap.getRssi()
                            + "\nBSSI: " + ap.getBssi()
                            + "\nOrientation: " + ap.getOrientation();
                    outputList.add(helperString);
                }
                /* Update the table */
                wifiArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void updateMeasurementsCount() {
                this.setPlaceIdString(editPlaceId.getText().toString());
                //* Sanity checks *//*
                if (!(placeIdString.isEmpty())) {
                    textViewMeasuresCount.setText(mMeasure.db.getMeasurementsNumberOfBssisForPlace(placeIdString));
                }
            }
        };

        mAverage = new AverageMeasures(this);


        wifiArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMeasure.outputList);
        wifiListView.setAdapter(wifiArrayAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wlan_localization:
                mLocalization.startLocalization();
                break;

            case R.id.wlan_scan:
                mMeasure.measureWlan();
                break;

            case R.id.wlan_calculate_averages:
                mAverage.calculateAverageMeasures();
                break;

            case R.id.wlan_delete_measurements_for_place:
                mMeasure.deleteAllMeasurementsForPlace();
                break;
        }
    }

    @Override
    protected void onPause() {
        mMeasure.stopScanningAndCloseProgressDialog();
        /*unbindService(mConnection);*/
        super.onPause();
    }

    /**************************************************************************/
    /*****************************   Bluetooth ********************************/
    /**************************************************************************/

    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    /*while(beacons.iterator().hasNext()) {*/
                    /*rssiArray[0] = beacons.iterator().next().getRssi();*/
                        /*measuredBeacon = beacons.iterator().next();*/
                         for(Beacon measuredBeacon : beacons) {
                             switch (measuredBeacon.getBluetoothAddress()) {
                                 case uuidBlue:
                                     rssiArray[0] = measuredBeacon.getRssi();
                                     Log.i(TAG, "+++Blaues Beacons" + rssiArray[0]);
                                     break;
                                 case uuidRed:
                                     rssiArray[1] = measuredBeacon.getRssi();
                                     Log.i(TAG, "+++Rotes Beacons" + rssiArray[1]);
                                     break;
                                 case uuidYellow:
                                     rssiArray[2] = measuredBeacon.getRssi();
                                     Log.i(TAG, "+++Gelbes Beacons" + rssiArray[1]);
                                     break;
                             }
                         }
                    /*}*/
                }
                mLocalization.bleAccess(rssiArray);

            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }
}
