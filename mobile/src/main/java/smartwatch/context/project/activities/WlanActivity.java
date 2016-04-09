package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import smartwatch.context.common.helper.WlanMeasurements;
import smartwatch.context.common.superclasses.AverageMeasures;
import smartwatch.context.common.superclasses.Localization;
import smartwatch.context.common.superclasses.Measure;
import smartwatch.context.project.R;


public class WlanActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "WlanActivity";

    private Localization mLocalization;
    private AverageMeasures mAverage;
    private Measure mMeasure;
    private EditText editPlaceId;
    private TextView textViewDebug;
    private TextView textViewMeasuresCount;
    private ArrayAdapter wifiArrayAdapter;
    protected WifiManager wifiManager;


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
        super.onPause();
    }
}
