package smartwatch.context.common.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CalculationHelper {
    /*private static final String TAG = "CalculationHelper";*/

    public static double calculateSse(List<WlanMeasurements> messwerte, List<WlanMeasurements> modellwerte) {
        /*modellwerte entsprechend dem jeweiligen Place der aufrufenden Methode*/
        boolean valueFound = false;
        double overallSum = 0;
        if (messwerte.size() == 0) {
            return 0;
        }

        for (WlanMeasurements modellwert : modellwerte) {
            for (WlanMeasurements messwert : messwerte) {
                if (messwert.getBssi().equals(modellwert.getBssi())) {
                    valueFound = true;
                    double diff = Math.abs(modellwert.getRssi() - messwert.getRssi());
                    overallSum = overallSum + diff;
                    /*If modellwert was found in messwerte, the search in messwerte
                    can be interrupted and we can continue with the search for the
                    next modellwert
                     */
                    break;
                }
            }

            if (!valueFound && modellwerte.indexOf(modellwert) < modellwerte.size() / 5) {
                overallSum += 20;
            }
            valueFound = false;
        }
        return overallSum / modellwerte.size();
    }

    public static Map.Entry<String, Double> minMapValue(HashMap<String, Double> sseMap) {
        Map.Entry<String, Double> min = null;
        if (sseMap != null) {
            for (Map.Entry<String, Double> entry : sseMap.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    if (entry.getValue() != 0) {
                        min = entry;
                    }
                }

            }
        }
        return min;
    }

    public static double sicherheitsWert(double ortsWert, HashMap<String, Double> sseMap) {
        double sumAbweichungen = 0;
        for (String key : sseMap.keySet()) {
            sumAbweichungen += Math.abs(ortsWert - sseMap.get(key));
        }
        return sumAbweichungen;
    }
}

