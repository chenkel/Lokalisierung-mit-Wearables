package smartwatch.context.common.helper;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CalculationHelper {
    private static final String TAG = "CalculationHelper";

    public static double calculateAverage(ArrayList<Integer> werte) {
        Integer sum = 0;
        if (!werte.isEmpty()) {
            for (Integer wert : werte) {
                sum += wert;
            }
        }
        return sum.doubleValue() / werte.size();
    }

    public static double calculateSse(List<WlanMeasurements> messwerte, List<WlanMeasurements> modellwerte) {
        /*modellwerte entsprechend dem jeweiligen Place der aufrufenden Methode*/
        boolean valueFound = false;
        int iteCounter = 0;
        /*Log.i(TAG, "Nun in calculate SSE");
        Log.i(TAG, "Größe Modellwerte: " + modellwerte.size());*/
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
                    /*Log.i(TAG, "#Modellwert gefunden. Die Daten sind: "+
                            modellwert.toString()+ "Und Diff ist "+diff);
                    Log.i(TAG, "#Die Zwischensumme ist: " + overallSum +
                        "\n-----------------------------------");*/
                }
            }

            if (!valueFound && modellwerte.indexOf(modellwert) < modellwerte.size() / 5) {
                /*Log.i(TAG, "!!!Modellwert nicht in Messwerten gefunden. Die Daten sind: "+
                    modellwert.toString());
                Log.i(TAG, "!!!Die Zwischensumme ist: " + overallSum +
                        "\n-----------------------------------");*/
                overallSum += 20;
            }
            /*else if(!valueFound){
                *//*Log.i(TAG, "***Modellwert nicht in Messwerten gefundenund nicht relevant: "+
                        modellwert.toString());*//*
            }*/
            valueFound = false;
            iteCounter++;
        }
        /*Log.i(TAG, "<---------------Overall Sum ist "+overallSum+"---------------->");
        Log.i(TAG, "<---------------Die Zahl der Iterationen ist "+iteCounter+"---------------->");*/
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

