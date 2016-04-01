package smartwatch.context.common.helper;

import java.util.Collection;
import java.util.Queue;

/**
 * Created by jan on 29.03.16.
 */
public class BleHelper {
    private static final String TAG = "BleHelper";

    public static double calculateAverage(Collection<Integer> queue) {
        double avgSum = 0;
        for (Integer element : queue) {
            avgSum += element;
        }
        return avgSum / queue.size();
    }

    public static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}
