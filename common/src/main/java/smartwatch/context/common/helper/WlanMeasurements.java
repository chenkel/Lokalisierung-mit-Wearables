package smartwatch.context.common.helper;

/**
 * Created by jan on 17.03.16.
 */
public class WlanMeasurements{

    private String bssi;
    private int rssi;
    private String ssid;
    private double orientation;


    public String getBssi() {
        return bssi;
    }

    public void setBssi(String bssi) {
        this.bssi = bssi;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public WlanMeasurements(String bssi, int rssi,String ssid,double orientation){
        this.bssi = bssi;
        this.rssi = rssi;
        this.ssid = ssid;
        this.orientation = orientation;
    }
    public WlanMeasurements(String bssi, int rssi, double orientation){
        this.bssi = bssi;
        this.rssi = rssi;
        this.orientation = orientation;
    }
    public WlanMeasurements(String bssi, int rssi, String ssid){
        this.bssi = bssi;
        this.rssi = rssi;
        this.ssid = ssid;
    }


    @Override
    public String toString() {
        return "WlanMeasurements{" +
                "bssi='" + bssi + '\'' +
                ", rssi=" + rssi +
                ", ssid='" + ssid + '\'' +
                ", orientation=" + orientation +
                '}';
    }

    /*public int compare(Object o1, Object o2)
    {
        WlanMeasurements p1 = (WlanMeasurements)o1;
        WlanMeasurements p2 = (WlanMeasurements)o2;
        // if last names are the same compare first names
        if(p1.getBssi().equals(p2.getBssi()))
        {
            return p1.getFirstName().compareTo(p2.getFirstName());
        }
        return p1.getLastName().compareTo(p2.getLastName());

    }*/

}
