package smartwatch.context.common.helper;

public class DataHelper {

    private double rssi;
    private String bssi;

    public DataHelper(String bssi, double rssi) {
        this.bssi = bssi;
        this.rssi = rssi;
    }

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

    @Override
    public String toString() {
        return "DataHelper{" +
                "rssi=" + rssi +
                ", bssi='" + bssi + '\'' +
                '}';
    }


}
