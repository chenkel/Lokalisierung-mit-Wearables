package smartwatch.context.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import smartwatch.context.common.superclasses.AverageMeasures;
import smartwatch.context.project.helper.DBManager;

public class MainPhoneActivity extends Activity implements View.OnClickListener, BeaconConsumer {
    /*private static final String TAG = MainPhoneActivity.class.getSimpleName();*/

    protected EditText editPlaceId;
    protected TextView textViewMeasuresCount;
    protected TextView textViewDebug;
    protected ArrayAdapter wifiArrayAdapter;
    protected WifiManager wifiManager;
    protected BeaconManager beaconManager;
    private PhoneLocalization mLocalization;
    private AverageMeasures mAverage;
    private PhoneMeasure mMeasure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_phone);

        setupWiFiAndBLE();

        mLocalization = new PhoneLocalization(this);

        mMeasure = new PhoneMeasure(this);

        mAverage = new AverageMeasures(this);

        initUI();
    }


    //region UI Initialization
    private void initUI() {
        initializeDebugTextView();
        initializeButtonListeners();
        initializePlaceIdField();
        registerArrayAdapterForDebugOutput();
    }

    private void initializeDebugTextView() {
    /* Initialize textViewDebug */
        textViewDebug = (TextView) findViewById(R.id.debug_text);
        textViewMeasuresCount = (TextView) findViewById(R.id.wlan_prev_scan_count);
    }

    private void initializeButtonListeners() {
        final Button buttonScan = (Button) findViewById(R.id.wlan_scan);
        buttonScan.setOnClickListener(this);

        final Button dbManagerButton = (Button) findViewById(R.id.db_manager);
        dbManagerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent dbManager = new Intent(view.getContext(), DBManager.class);
                startActivity(dbManager);
            }
        });

        /* Initialize buttonCalculateAverages and its listener for the button */
        final Button buttonCalculateAverages = (Button) findViewById(R.id.wlan_calculate_averages);
        buttonCalculateAverages.setOnClickListener(this);

        /* Initialize buttonCalculateAverages and its listener for the button */
        final Button buttonLocalization = (Button) findViewById(R.id.wlan_localization);
        buttonLocalization.setOnClickListener(this);

        /* Initialize buttonDeleteAllMeasurements and its listener for the button */
        final Button buttonDeleteAllMeasurements = (Button) findViewById(R.id.wlan_delete_measurements_for_place);
        buttonDeleteAllMeasurements.setOnClickListener(this);
    }

    private void initializePlaceIdField() {
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
    }

    private void registerArrayAdapterForDebugOutput() {
        /* Register components from activity */
        final ListView wifiListView = (ListView) findViewById(R.id.listViewWifi);
        /* Setup ArrayAdapter displaying scan results */
        wifiArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMeasure.outputList);
        wifiListView.setAdapter(wifiArrayAdapter);
    }

    public void onClick(View view) {
        reactToMenuClick(view);
    }

    private void reactToMenuClick(View view) {
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
    //endregion

    //region WiFi and BLE Setup
    private void setupWiFiAndBLE() {
        initializeBeaconManager();
        checkIfWiFiIsEnabled();
    }


    private void checkIfWiFiIsEnabled() {
        /* Enable Wi-Fi, if necessary */
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "WLAN wird eingeschaltet...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    private void initializeBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(mLocalization.rangeNotifier);

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingWatchId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //endregion


    @Override
    protected void onPause() {
        beaconManager.unbind(this);
        mMeasure.stopMeasuring();
        super.onPause();
    }
}
