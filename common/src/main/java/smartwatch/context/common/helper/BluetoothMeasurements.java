package smartwatch.context.common.helper;

/**
 * Created by jan on 07.04.16.
 */
public class BluetoothMeasurements {
    String name;
    int rssi;
    String uuid;
    String[] places;
    double distance;

    public BluetoothMeasurements(String name, int rssi, String uuid, String[] places){
        this.name=name;
        this.rssi=rssi;
        this.uuid=uuid;
        this.places=places;
    }

    public BluetoothMeasurements(String name, String[] places){
        this.name=name;
        this.places=places;
    }

}
