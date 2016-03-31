package smartwatch.context.common.helper;

import java.util.List;

/**
 * Created by jan on 17.03.16.
 */
public class DataHelper {

    private double rssi;
    private String bssi;

    public String getBssi() {
        return bssi;
    }

    public void setBssi(String bssi) {
        this.bssi = bssi;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }


    public DataHelper(String bssi, double rssi){
        this.bssi = bssi;
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "DataHelper{" +
                "rssi=" + rssi +
                ", bssi='" + bssi + '\'' +
                '}';
    }


}
