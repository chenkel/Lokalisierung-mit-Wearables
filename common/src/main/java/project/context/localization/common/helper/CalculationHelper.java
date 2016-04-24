package project.context.localization.common.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The type Calculation helper.
 */
public class CalculationHelper {

    /**
     * Calculates the distance of a current WiFi measurement to the stored storedAPMeasurements to
     * find out the lowest value and thereby closest location to the current measurements.
     *
     * @param wiFiMeasurements   the currently scanned measurements
     * @param storedAPMeasurements the stored access point measurement of the WiFi access points
     * @return the double of the sum of distance between the stored
     * RSSI and the current one for all storedAPMeasurements
     */
    public static double calculateDistance(List<WiFiMeasurement> wiFiMeasurements, List<WiFiMeasurement> storedAPMeasurements) {
        boolean valueFound = false;
        double overallSum = 0;
        if (wiFiMeasurements.size() == 0) {
            return 0;
        }

        for (WiFiMeasurement ap : storedAPMeasurements) {
            for (WiFiMeasurement wiFiMeasurement : wiFiMeasurements) {
                if (wiFiMeasurement.getBssi().equals(ap.getBssi())) {
                    valueFound = true;
                    double diff = Math.abs(ap.getRssi() - wiFiMeasurement.getRssi());
                    overallSum = overallSum + diff;
                    /* If ap's bssi was found in wiFiMeasurements, the search in
                    wiFiMeasurements can be interrupted and we can
                    continue with the calculation of the next ap
                     */
                    break;
                }
            }
            /* Punishment for not seeing the AP belonging to the
            Top 5 of APs at the current location */
            if (!valueFound && storedAPMeasurements.indexOf(ap) < storedAPMeasurements.size() / 5) {
                overallSum += 20;
            }
            valueFound = false;
        }
        return overallSum / storedAPMeasurements.size();
    }

    /**
     * Returns the minimum entry of the placeDistanceMap
     *
     * @param placeDistanceMap the map containing the distance to all
     *                         places from the last position of the measurement
     * @return the minimum entry of the placeDistanceMap
     */
    public static Map.Entry<String, Double> minMapValue(HashMap<String, Double> placeDistanceMap) {
        Map.Entry<String, Double> min = null;
        if (placeDistanceMap != null) {
            for (Map.Entry<String, Double> entry : placeDistanceMap.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    if (entry.getValue() != 0) {
                        min = entry;
                    }
                }

            }
        }
        return min;
    }

    /**
     * The security value is the sum of deviations between the distance
     * of one specific place and all the other ones in the placeDistanceMap.
     *
     * @param distanceAtOnePlace the distance for one specific place
     * @param placeDistanceMap   the place distance map that the single gets compared to
     * @return the double value of the sum of deviation, also called "security value"
     */
    public static double securityValue(double distanceAtOnePlace, HashMap<String, Double> placeDistanceMap) {
        double sumDeviation = 0;
        for (String key : placeDistanceMap.keySet()) {
            sumDeviation += Math.abs(distanceAtOnePlace - placeDistanceMap.get(key));
        }
        return sumDeviation;
    }
}

