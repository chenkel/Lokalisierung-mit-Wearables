package smartwatch.context.common.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by jan on 07.04.16.
 */
public class BluetoothData {

    private Queue<Integer> rssiQueueBlue = new LinkedList<>();
    private Queue<Integer> rssiQueueYellow = new LinkedList<>();
    private Queue<Integer> rssiQueueRed = new LinkedList<>();

    private final String uuidBlue = "CE:BA:BE:97:DB:0C";
    private final String uuidYellow = "FB:39:E6:2D:82:EF";
    private final String uuidRed = "DD:3F:50:F2:76:74";

    private double avgBlue = 0;
    private double avgYellow = 0;
    private double avgRed = 0;

    private final int queueSize = 5;

    public BluetoothData(Queue<Integer> blue, Queue<Integer> yellow, Queue<Integer> red){
        rssiQueueBlue = blue;
        rssiQueueYellow = yellow;
        rssiQueueRed = red;
    }

    public Map<String, Number> queueAssignment(String uuid, int rssi) {
        switch (uuid) {
            case uuidBlue:
                if (rssiQueueBlue.size() < queueSize) {
                    rssiQueueBlue.add(rssi);
                } else if (rssiQueueBlue.size() >= queueSize) {
                    rssiQueueBlue.remove();
                    rssiQueueBlue.add(rssi);
                }
                break;

            case uuidYellow:
                if (rssiQueueYellow.size() < queueSize) {
                    rssiQueueYellow.add(rssi);
                } else if (rssiQueueYellow.size() >= queueSize) {
                    rssiQueueYellow.remove();
                    rssiQueueYellow.add(rssi);
                }
                break;

            case uuidRed:
                if (rssiQueueRed.size() < queueSize) {
                    rssiQueueRed.add(rssi);
                } else if (rssiQueueRed.size() >= queueSize) {
                    rssiQueueRed.remove();
                    rssiQueueRed.add(rssi);
                }
                break;
        }

        avgBlue = calculateAverage(rssiQueueBlue);
        avgYellow = calculateAverage(rssiQueueYellow);
        avgRed = calculateAverage(rssiQueueRed);

        Map<String, Number> avgValues = new HashMap<String, Number>();
        avgValues.put("blue", avgBlue);
        avgValues.put("yellow", avgYellow);
        avgValues.put("red", avgRed);


        return avgValues;
    }

    public double calculateAverage(Collection<Integer> queue) {
        double avgSum = 0;
        for (Integer element : queue) {
            avgSum += element;
        }
        return avgSum / queue.size();
    }
}
