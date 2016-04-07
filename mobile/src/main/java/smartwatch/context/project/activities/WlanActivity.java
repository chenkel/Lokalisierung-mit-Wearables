package smartwatch.context.project.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import java.util.Collections;
import java.util.Comparator;

import smartwatch.context.common.helper.WlanMeasurements;
import smartwatch.context.common.superclasses.CommonActivity;
import smartwatch.context.project.R;


public class WlanActivity extends CommonActivity implements View.OnClickListener {
    private static final String TAG = "WlanActivity";

    private EditText editPlaceId;
    private ArrayAdapter wifiArrayAdapter;
    private ProgressDialog progress;

    @Override
    protected void updateProgressOutput(int scanCount) {
        progress.setProgress(scanCount);
    }

    @Override
    protected void hideProgressOutput() {
        if (progress != null) {
            progress.hide();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlan);
        Log.v(TAG, "WlanActivity constructor called.");

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
                updateMeasurementsCount();
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

        /* Initialize textViewAverages */
        textViewAverages = (TextView) findViewById(R.id.averages_text);

        /* Setup ArrayAdapter displaying scan results */

        wifiArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, outputList);
        wifiListView.setAdapter(wifiArrayAdapter);


    }

    @Override
    protected void showScanProgress() {
        progress = new ProgressDialog(WlanActivity.this);

        progress.setTitle("Scan der WLAN-Umgebung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    @Override
    protected void showLocalizationProgressDialog() {
        progress = new ProgressDialog(WlanActivity.this);

        progress.setTitle("Lokalisierung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setMax(scanCountMax);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.w(TAG, "Dialog canceled");
                stopScanningAndCloseProgressDialog();
            }
        });
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }

    @Override
    protected void updateLocalizationProgressUI(String foundPlaceId, String waypointDescription) {
        progress.setTitle("Ort: " + foundPlaceId);
        progress.setMessage(waypointDescription);
    }

    @Override
    protected void notifyLocationChange() {
// Vibrate for 500 milliseconds if place changed
        v.vibrate(500);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wlan_localization:
                startLocalization();
                break;

            case R.id.wlan_scan:
                scanWlan();
                break;

            case R.id.wlan_calculate_averages:
                new DoCalculationTask().execute();
                break;

            case R.id.wlan_delete_measurements_for_place:
                deleteAllMeasurementsForPlace();
                break;
        }
    }


    @Override
    protected void updateMeasurementsCount() {
        placeIdString = editPlaceId.getText().toString();
        /* Sanity checks */
        if (!(placeIdString.isEmpty())) {
            TextView textViewMCount = (TextView) findViewById(R.id.wlan_prev_scan_count);
            textViewMCount.setText(measurementDB.getNumberOfBssisForPlace(placeIdString));
        }
    }

    @Override
    protected void outputDebugInfos() {
        /* ONLY NEEDED FOR DEBUGGING ON PHONE */

        /*Sorting of WlanMeasurements*/
        Comparator<WlanMeasurements> wlanComparator = new Comparator<WlanMeasurements>() {
            @Override
            public int compare(WlanMeasurements lhs, WlanMeasurements rhs) {
                return (lhs.getRssi() > rhs.getRssi() ? -1 : (lhs.getRssi() == rhs.getRssi() ? 0 : 1));
            }
        };

        Collections.sort(wlanMeasure, wlanComparator);

        /*only show last measurement in list*/
        for (WlanMeasurements ap : wlanMeasure) {
            String helperString = "SSID: " + ap.getSsid()
                    + "\nRSSI: " + ap.getRssi()
                    + "\nBSSI: " + ap.getBssi()
                    + "\nOrientation: " + ap.getOrientation();
            outputList.add(helperString);
        }
        /*Update the table*/
        wifiArrayAdapter.notifyDataSetChanged();
                    /* -- END: ONLY NEEDED FOR DEBUGGING ON PHONE */
    }

    @Override
    protected void showScansSaveProgress() {
        progress.setTitle("Alle Scandaten werden gespeichert");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(wlanMeasure.size());
        progress.show();
    }

    @Override
    protected void setMaxProgressOutput(int count) {
        progress.setMax(count);
    }

    @Override
    protected void showCalculationProgressOutput() {
        progress.setTitle("Durchschnittliche Signalstärke aller APs für verschiede Orte wird berechnet");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    @Override
    protected void outputDetailedPlaceInfoDebug(String output) {
        textViewAverages.setText(output);
    }


}
