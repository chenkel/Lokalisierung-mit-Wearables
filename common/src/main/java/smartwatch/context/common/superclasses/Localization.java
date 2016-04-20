package smartwatch.context.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartwatch.context.common.helper.CalculationHelper;
import smartwatch.context.common.helper.WlanMeasurements;

public abstract class Localization extends CommonClass {
    private static final String TAG = Localization.class.getSimpleName();
    private List<WlanMeasurements> wlanMeasure = new ArrayList<>();

    private String priorPlaceId = "";

    private final String uuidBlue = "CE:BA:BE:97:DB:0C";
    private final String uuidRed = "DD:3F:50:F2:76:74";
    private final String uuidYellow = "FB:39:E6:2D:82:EF";

    private Integer blueRssi = -200;
    private Integer redRssi = -200;
    private Integer yellowRssi = -200;

    private String[] bluePlaces = {"1"};
    private String[] redPlaces = {"3"};
    private String[] yellowPlaces = {"5"};

    public RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            Log.i(TAG, "didRangeBeaconsInRegion");
            if (beacons.size() > 0) {
                for (Beacon measuredBeacon : beacons) {
                    switch (measuredBeacon.getBluetoothAddress()) {
                        case uuidBlue:
                            setBlueRssi(measuredBeacon.getRssi());
                            Log.i(TAG, "+++Blaues Beacons");
                            break;
                        case uuidRed:
                            setRedRssi(measuredBeacon.getRssi());
                            Log.i(TAG, "+++Rotes Beacons");
                            break;
                        case uuidYellow:
                            setYellowRssi(measuredBeacon.getRssi());
                            Log.i(TAG, "+++Gelbes Beacons");
                            break;
                    }
                    Log.i(TAG, "RSSI: " + measuredBeacon.getRssi());
                }
            }
        }
    };


    protected final BroadcastReceiver localizationScanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> currentResults = wifiManager.getScanResults();

            int measurementCount = currentResults.size();
            if (measurementCount > 0) {
                for (ScanResult result : currentResults) {
                    wlanMeasure.add(new WlanMeasurements(
                            result.BSSID,
                            result.level,
                            result.SSID,
                            0
                    ));
                }

                locateUser();
                wifiManager.startScan();

            } else {
                stopLocalization();

                Toast.makeText(context, "Keine APs in der Umgebung gefunden", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public Localization(Activity activity) {
        super(activity);
        setResultReceiver(localizationScanResultReceiver);
    }

    public void startLocalization() {
        outputList.clear();
        wlanMeasure.clear();

        /* Register Listener to collect results */
        getActivity().registerReceiver(localizationScanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        showLocalizationProgressOutput();
    }

    public void stopLocalization() {
        try {
            /* Stop the continous scan */
            getActivity().unregisterReceiver(localizationScanResultReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, e.toString());
        } finally {
            /* Hide the loading spinner */
            hideProgressOutput();
        }
    }

    protected void showLocalizationProgressOutput() {
        progress = new ProgressDialog(getActivity());

        progress.setTitle("Lokalisierung läuft");
        progress.setMessage("Bitte warten Sie einen Moment...");
        progress.setProgress(0);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.w(TAG, "Dialog canceled");
                stopLocalization();
            }
        });
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }

    protected void locateUser() {
        if (wlanMeasure.size() == 0) {
            Toast.makeText(getActivity(), "Bitte führen sie eine Messung durch", Toast.LENGTH_SHORT).show();
            stopLocalization();
            return;
        }

        /*Saves all places to placeList*/
        Cursor placeCursor = db.getAllDistinctPlacesFromMeasurements();
        if (placeCursor.getCount() == 0) {
            Toast.makeText(getActivity(), "Keine Messdaten vorhanden", Toast.LENGTH_SHORT).show();
            stopLocalization();
            return;
        }

        ArrayList<String> placeList = new ArrayList<>();
        boolean beaconsFound = false;
        placeList.clear();

        /* Anpassung der placeList abhängig von empfangenen Bluetooth Beacons */
        if (blueRssi > -70) {
            Collections.addAll(placeList, bluePlaces);
            beaconsFound = true;
        }

        if (redRssi > -70) {
            Collections.addAll(placeList, redPlaces);
            beaconsFound = true;
        }

        if (yellowRssi > -75) {
            Collections.addAll(placeList, yellowPlaces);
            beaconsFound = true;
        }

        if (!beaconsFound) {
            for (placeCursor.moveToFirst(); !placeCursor.isAfterLast(); placeCursor.moveToNext()) {
                placeList.add(placeCursor.getString(0));
            }
            placeCursor.close();
        }


        /*Am Ende wird jedem Ort eine sse zugeordnet*/
        HashMap<String, Double> sseMap = new HashMap<>();
        List<WlanMeasurements> modellWerte = new ArrayList<>();

        for (String place : placeList) {
            /*Get all BSSI and corresponding RSSI for place*/
            modellWerte.clear();

            /*Fills the cursor with all BSSIDs and their RSSIs at the place*/
            Cursor modelCursor = db.getAverageRssiByPlace(place);
            /* Fügt in Model Data List dem Key bssi den Value rssi aus Datenbank hinzu
            bssi,rssi, ssid
            */
            for (modelCursor.moveToFirst(); !modelCursor.isAfterLast(); modelCursor.moveToNext()) {
                WlanMeasurements messWert = new WlanMeasurements(modelCursor.getString(0),
                        modelCursor.getInt(1), modelCursor.getString(2));
                modellWerte.add(messWert);
            }
            modelCursor.close();

            double sseValue = CalculationHelper.calculateSse(wlanMeasure, modellWerte);
            sseMap.put(place, sseValue);
        }

        if (!sseMap.isEmpty()) {
            Map.Entry<String, Double> minEntry = CalculationHelper.minMapValue(sseMap);
            if (minEntry != null) {
                String foundPlaceId = minEntry.getKey();
                String outputTextview = "Der Ort ist: " + foundPlaceId + " mit Wert: " + minEntry.getValue() + "\n";

                /*Toast.makeText(context, "Ort: " + foundPlaceId, Toast.LENGTH_SHORT).show();*/
                if (!priorPlaceId.equals(foundPlaceId)) {
                    if ((priorPlaceId.equals("1") && foundPlaceId.equals("2")) ||
                            (priorPlaceId.equals("2") && foundPlaceId.equals("1")) ||
                            (priorPlaceId.equals("3") && foundPlaceId.equals("4")) ||
                            (priorPlaceId.equals("4") && foundPlaceId.equals("3")) ||
                            (priorPlaceId.equals("5") && foundPlaceId.equals("6")) ||
                            (priorPlaceId.equals("6") && foundPlaceId.equals("5"))) {
                        Log.d(TAG, "PlaceId changed but description stays the same");
                    } else {
                        notifyLocationChange(priorPlaceId, foundPlaceId);
                        updateLocalizationProgressUI(foundPlaceId, getLocationDescription(foundPlaceId));
                    }
                }
                priorPlaceId = foundPlaceId;

                /*Mit steigendem Sicherheitswert ist der sse klein für die Lokation und groß für
                die anderen Orte
                 */
                double sicherheitSse = CalculationHelper.sicherheitsWert(minEntry.getValue(), sseMap);
                String sicherheitString = "Der Sicherheitswert ist: " + sicherheitSse + "\n";

                StringBuilder sbSse = new StringBuilder();
                for (String key : sseMap.keySet()) {
                    sbSse.append(key).append(": ");
                    double sseValue = Math.round(sseMap.get(key) * 1000);
                    sseValue = sseValue / 1000;
                    sbSse.append(sseValue).append("\n");
                }
                String textViewAveragesString = outputTextview + sicherheitString + sbSse.toString();
                outputDetailedPlaceInfoDebug(textViewAveragesString);
            } else {
                Toast.makeText(getActivity(), "Durchschnittswerte fehlen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "sseMap empty");
        }

        wlanMeasure.clear();
    }

    protected void updateLocalizationProgressUI(String foundPlaceId, String locationDescription) {
        progress.setTitle("Ort: " + foundPlaceId);
        progress.setMessage(locationDescription);
    }

    protected String getLocationDescription(String foundPlaceId) {
        String sDescription = "";
        switch (foundPlaceId) {
            case "1":
                sDescription = "Verlasse das Zimmer und gehe nach links";
                break;
            case "2":
                sDescription = "Verlasse das Zimmer und gehe nach links";
                break;
            case "3":
                sDescription = "Gehe durch die Glastür";
                break;
            case "4":
                sDescription = "Gehe durch die Glastür";
                break;
            case "5":
                sDescription = "Gehe nach rechts";
                break;
            case "6":
                sDescription = "Gehe nach rechts";
                break;
            case "7":
                sDescription = "Gehe durch die Notfalltür";
                break;
            case "8":
                sDescription = "Gehe durch die Notfalltür";
                break;
        }
        return sDescription;
    }

    protected abstract void notifyLocationChange(String priorPlaceId, String foundPlaceId);

    protected void outputDetailedPlaceInfoDebug(String output) {
        /*Log.i(TAG, output);*/
    }

    public void setBlueRssi(Integer blueRssi) {
        this.blueRssi = blueRssi;
    }

    public void setRedRssi(Integer redRssi) {
        this.redRssi = redRssi;
    }

    public void setYellowRssi(Integer yellowRssi) {
        this.yellowRssi = yellowRssi;
    }
}
