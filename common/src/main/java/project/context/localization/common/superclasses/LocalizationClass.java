package project.context.localization.common.superclasses;

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

import project.context.localization.common.helper.CalculationHelper;
import project.context.localization.common.helper.PositionsHelper;
import project.context.localization.common.helper.WiFiMeasurement;

/**
 * The abstract class LocalizationClass offers methods and attributes to locate a user by
 * two methods: WiFi-Fingerprinting and Bluetooth Beacons.
 */
public abstract class LocalizationClass extends CommonClass {
    private static final String TAG = LocalizationClass.class.getSimpleName();
    private final List<WiFiMeasurement> wlanMeasure = new ArrayList<>();
    private String priorPlaceId = "";

    private Integer blueRssi = -200; // initial value lower than any real value (here: rssi <= -100)
    private Integer redRssi = -200;
    private Integer yellowRssi = -200;

    /**
     * The blue beacon's minor.
     */
    final String blueMinor = "1";
    /**
     * The red beacon's minor.
     */
    final String redMinor = "2";
    /**
     * The yellow beacon's minor.
     */
    final String yellowMinor = "3";
    /**
     * The Range notifier gets called when beacon signals are found in the proximity of the device.
     * The method basically stores the RSSI of the corresponding beacon.
     * The RSSI information is later used in {@link #findClosestPlaceIdWithScanResults()} to restrict the place list to
     * the known ones in the beacon cell.
     */
    public final RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                for (Beacon measuredBeacon : beacons) {
                    String beaconMajor = "10";
                    if (beaconMajor.equals(measuredBeacon.getIdentifier(1).toString())) {

                        switch (measuredBeacon.getIdentifier(2).toString()) {
                            case blueMinor:
                                setBlueRssi(measuredBeacon.getRssi());
                                Log.i(TAG, "0++++ Blaues Beacons");
                                break;

                            case redMinor:
                                setRedRssi(measuredBeacon.getRssi());
                                Log.i(TAG, "++0++ Rotes Beacons");
                                break;

                            case yellowMinor:
                                setYellowRssi(measuredBeacon.getRssi());
                                Log.i(TAG, "++++0 Gelbes Beacons");
                                break;

                            default:
                                Log.d(TAG, "Beacon Minor ist unbekannt");
                                break;
                        }
                        Log.i(TAG, "RSSI: " + measuredBeacon.getRssi());
                    } else {
                        Log.d(TAG, "Beacon Major ist unbekannt");
                    }
                }
            }
        }
    };

    /**
     * The BroadcastReceiver gets the results of the WiFi Scan and
     * adds all the results to wiFiMeasurements.
     * After that find out the closest place in the db to the current one and
     * start another wifi scan (continous)
     */
    private final BroadcastReceiver localizationScanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> currentResults = wifiManager.getScanResults();

            int measurementCount = currentResults.size();
            if (measurementCount > 0) {
                for (ScanResult result : currentResults) {
                    wlanMeasure.add(new WiFiMeasurement(result.BSSID, result.level, result.SSID));
                }

                findClosestPlaceIdWithScanResults();
                wifiManager.startScan();

            } else {
                stopLocalization();

                Toast.makeText(context, "Keine APs in der Umgebung gefunden", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Instantiates the LocalizationClass.
     *
     * @param activity the activity reference of the instantiating activity
     */
    public LocalizationClass(Activity activity) {
        super(activity);
    }

    /**
     * Triggers the continous localization of the user by scanning the WiFi and BLE Beacons.
     * A Progress output is shown to the user to display the start of the localization process.
     */
    public void startLocalization() {

        wlanMeasure.clear();

        /* Register Listener to collect results */
        getActivity().registerReceiver(localizationScanResultReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        /* Starts the wifi scanner */
        wifiManager.startScan();

        showLocalizationProgressOutput();
    }

    /**
     * Stop the continous localization and unregister the scanResultReceiver
     * and hide the progress output.
     */
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

    /**
     * Show the progress output for the initial localization process.
     */
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

    /**
     * This method is the core of the localization process.
     * First all place ids are fetched.
     * Then the total distance (deviation in rssi) between
     * the current measurement and the stored measurement of all places is calculated.
     * In the end the smallest distance to the current measurement is notified to the user.
     */
    private void findClosestPlaceIdWithScanResults() {
        /* Sanity checks */
        if (wlanMeasure.size() == 0) {
            Toast.makeText(getActivity(), "Bitte führen sie eine Messung durch", Toast.LENGTH_SHORT).show();
            stopLocalization();
            return;
        }
        ArrayList<String> placeList = getPlacesList();
        if (placeList == null) return;
        HashMap<String, Double> placeDistanceMap = calculateTotalDistanceToEveryPlace(placeList);

        String foundPlaceId = findMinimalDistance(placeDistanceMap);

        if (foundPlaceId != null) {
            compareWithPriorPlaceAndNotify(priorPlaceId, foundPlaceId);
        }
        priorPlaceId = foundPlaceId;

        wlanMeasure.clear();
    }

    /**
     * getPlacesList returns either a filtered place list by the found beacon cell or
     * returns all of the measured places.
     *
     * @return a list of places to test the current position against
     */
    private ArrayList<String> getPlacesList() {

        Cursor placeCursor = db.getAllDistinctPlacesFromMeasurements();
        if (placeCursor.getCount() == 0) {
            Toast.makeText(getActivity(), "Keine Messdaten vorhanden", Toast.LENGTH_SHORT).show();
            stopLocalization();
            return null;
        }

        ArrayList<String> placeList = new ArrayList<>();
        boolean beaconsFound = false;
        placeList.clear();

        /* Extend placeList by predefined list of placeIds for individual beacons */
        if (blueRssi > -70) {
            Collections.addAll(placeList, PositionsHelper.bluePlaces);
            beaconsFound = true;
        }

        if (redRssi > -70) {
            Collections.addAll(placeList, PositionsHelper.redPlaces);
            beaconsFound = true;
        }

        if (yellowRssi > -75) {
            Collections.addAll(placeList, PositionsHelper.yellowPlaces);
            beaconsFound = true;
        }

        if (!beaconsFound) {
            /* Saves all places to placeList */
            for (placeCursor.moveToFirst(); !placeCursor.isAfterLast(); placeCursor.moveToNext()) {
                placeList.add(placeCursor.getString(0));
            }
        }
        placeCursor.close();

        return placeList;
    }

    /**
     * Calculates the distance to every place given in the parameter and returns it as a map.
     *
     * @param placeList is the list of all relevant and potential places for the calculation
     * @return a map of a place id and its corresponding distance to the current measurement
     */
    private HashMap<String, Double> calculateTotalDistanceToEveryPlace(ArrayList<String> placeList) {
    /*Am Ende wird jedem Ort eine sse zugeordnet*/
        HashMap<String, Double> placeDistanceMap = new HashMap<>();
        List<WiFiMeasurement> wiFiMeasurementsList = new ArrayList<>();

        for (String place : placeList) {
            /*Get all BSSI and corresponding RSSI for place*/
            wiFiMeasurementsList.clear();

            /*Fills the cursor with all BSSIDs and their RSSIs at the place*/
            Cursor avgRssiCursor = db.getAverageRssiByPlace(place);
            /* Fügt in Model Data List dem Key bssi den Value rssi aus Datenbank hinzu
            bssi,rssi, ssid
            */
            for (avgRssiCursor.moveToFirst(); !avgRssiCursor.isAfterLast(); avgRssiCursor.moveToNext()) {
                WiFiMeasurement wiFiMeasurement = new WiFiMeasurement(avgRssiCursor.getString(0),
                        avgRssiCursor.getInt(1), avgRssiCursor.getString(2));
                wiFiMeasurementsList.add(wiFiMeasurement);
            }
            avgRssiCursor.close();

            double totalDistance = CalculationHelper.calculateDistance(wlanMeasure, wiFiMeasurementsList);
            placeDistanceMap.put(place, totalDistance);
        }
        return placeDistanceMap;
    }

    /**
     * Finds the minimum distance of the place distance map
     *
     * @param placeDistanceMap contains every distance to the places from the current position.
     * @return a String that contains the place with the minimum distance
     */
    private String findMinimalDistance(HashMap<String, Double> placeDistanceMap) {
        if (!placeDistanceMap.isEmpty()) {
            Map.Entry<String, Double> minEntry = CalculationHelper.minMapValue(placeDistanceMap);
            if (minEntry != null) {
                String foundPlaceId = minEntry.getKey();

                debugLocalization(placeDistanceMap, minEntry, foundPlaceId);
                return foundPlaceId;
            } else {
                Toast.makeText(getActivity(), "Durchschnittswerte fehlen", Toast.LENGTH_SHORT).show();
                return null;
            }
        } else {
            Log.e(TAG, "placeDistanceMap empty");
            return null;
        }
    }

    /**
     * debugLocalization helps to understand and monitor the algorithm
     * to find the closest place from the current measurement.
     *
     * @param placeDistanceMap      holds a map containing the place and the corresponding distance
     * @param minPlaceDistanceEntry is the closest place to the current position or measurement
     * @param foundPlaceId          is the id string of the closest place id
     */
    private void debugLocalization(HashMap<String, Double> placeDistanceMap, Map.Entry<String, Double> minPlaceDistanceEntry, String foundPlaceId) {
        String outputTextview = "Der Ort ist: " + foundPlaceId + " mit Wert: " + minPlaceDistanceEntry.getValue() + "\n";

                /*Mit steigendem Sicherheitswert ist der sse klein für die Lokation und groß für
                die anderen Orte
                 */
        double sicherheitSse = CalculationHelper.securityValue(minPlaceDistanceEntry.getValue(), placeDistanceMap);
        String sicherheitString = "Der Sicherheitswert ist: " + sicherheitSse + "\n";

        StringBuilder sbSse = new StringBuilder();
        for (String key : placeDistanceMap.keySet()) {
            sbSse.append(key).append(": ");
            double sseValue = Math.round(placeDistanceMap.get(key) * 1000);
            sseValue = sseValue / 1000;
            sbSse.append(sseValue).append("\n");
        }
        String textViewAveragesString = outputTextview + sicherheitString + sbSse.toString();
        outputDetailedPlaceInfoDebug(textViewAveragesString);
    }

    /**
     * Compares the prior place to the new one and notifies if the zone of the place changed.
     * Zones are separated by 10 to the power of x, where is x is the zone id.
     *
     * @param priorPlaceId prior place id
     * @param foundPlaceId current (found) place id
     */
    private void compareWithPriorPlaceAndNotify(String priorPlaceId, String foundPlaceId) {

        boolean zoneChanged = PositionsHelper.isZoneDifferentWithPriorAndCurrentPlace(priorPlaceId, foundPlaceId);

        if ((zoneChanged)) {
            notifyLocationChange(priorPlaceId, foundPlaceId);
            updateLocalizationProgressUI(foundPlaceId, PositionsHelper.getCurrentZoneDescription());
        } else {
            Log.d(TAG, "Place changed but zone is still the same");
        }
    }

    /**
     * Update localization progress ui.
     *
     * @param foundPlaceId        the found place id
     * @param locationDescription the location description
     */
    protected void updateLocalizationProgressUI(String foundPlaceId, String locationDescription) {
        progress.setTitle("Ort: " + foundPlaceId);
        progress.setMessage(locationDescription);
    }


    /**
     * Notify location change.
     *
     * @param priorPlaceId the prior place id
     * @param foundPlaceId the found place id
     */
    protected abstract void notifyLocationChange(String priorPlaceId, String foundPlaceId);

    /**
     * Output detailed debugging information debug.
     *
     * @param output the output showing the found place,               its deviations of other APs and               an confidence value for the found place
     */
    protected void outputDetailedPlaceInfoDebug(String output) {
        /*Log.i(TAG, output);*/
    }

    private void setBlueRssi(Integer blueRssi) {
        this.blueRssi = blueRssi;
    }

    private void setRedRssi(Integer redRssi) {
        this.redRssi = redRssi;
    }

    private void setYellowRssi(Integer yellowRssi) {
        this.yellowRssi = yellowRssi;
    }
}
