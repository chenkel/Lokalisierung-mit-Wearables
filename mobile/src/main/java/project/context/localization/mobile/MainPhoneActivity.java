package project.context.localization.mobile;

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

import java.util.ArrayList;
import java.util.List;

import project.context.localization.common.superclasses.AverageMeasuresClass;
import project.context.localization.mobile.helper.DBManager;

/**
 * The type Main phone activity presents the UI Elements to do all
 * necessary steps for preparing and executing the localization of the user.
 *
 * It was more or less the playground to quickly develop an Android-based solution, which
 * was easily portable to other device categories (i.e. Wearables)
 */
public class MainPhoneActivity extends Activity implements View.OnClickListener, BeaconConsumer {
    EditText editPlaceId;
    TextView textViewMeasuresCount;
    TextView textViewDebug;

    ArrayAdapter wifiArrayAdapter;
    private BeaconManager beaconManager;
    private PhoneLocalizationClass mLocalization;
    private AverageMeasuresClass mAverage;
    private PhoneMeasureClass mMeasure;

    /**
     * The Debug output list shows information about scanned WiFi APs.
     */
    protected List<String> debugOutputList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_phone);

        setupWiFiAndBLE();

        mLocalization = new PhoneLocalizationClass(this);
        mMeasure = new PhoneMeasureClass(this);
        mAverage = new AverageMeasuresClass(this);

        debugOutputList = new ArrayList<>();

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

    /**
     * Initialise PlaceId field.
     *
     * Listens to textChanges and updates the measurement count accordingly.
     */
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
        wifiArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, debugOutputList);
        wifiListView.setAdapter(wifiArrayAdapter);
    }

    /**
     * onClick Listener for the view object.
     * @param view the main view
     */
    public void onClick(View view) {
        reactToMenuClick(view);
    }

    private void reactToMenuClick(View view) {
        switch (view.getId()) {
            case R.id.wlan_localization:
                debugOutputList.clear();
                mLocalization.startLocalization();
                break;

            case R.id.wlan_scan:
                debugOutputList.clear();
                mMeasure.measureWiFi();
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
    /**
     * Setup the WifiManager and BeaconManager.
     */
    private void setupWiFiAndBLE() {
        initializeBeaconManager();
        checkIfWiFiIsEnabled();
    }

    /**
     * Initialize the WifiManager and activate the WiFi on the device.
     */
    private void checkIfWiFiIsEnabled() {
        /* Enable Wi-Fi, if necessary */
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "WLAN wird eingeschaltet...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * Initialize the BeaconManager to receive new beacons in range.
     */
    private void initializeBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
    }

    /**
     * Set the range notifier in onBeaconServiceConnect and start looking for beacons.
     */
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
