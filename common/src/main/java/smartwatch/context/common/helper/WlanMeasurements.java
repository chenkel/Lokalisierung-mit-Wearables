package smartwatch.context.common.helper;


public class WlanMeasurements {

    private final String bssi;
    private final int rssi;
    private final String ssid;


    public WlanMeasurements(String bssi, int rssi, String ssid) {
        this.bssi = bssi;
        this.rssi = rssi;
        this.ssid = ssid;
    }

    public String getBssi() {
        return bssi;
    }

    public int getRssi() {
        return rssi;
    }

    public String getSsid() {
        return ssid;
    }

    @Override
    public String toString() {
        return "WlanMeasurements{" +
                "bssi='" + bssi + '\'' +
                ", rssi=" + rssi +
                ", ssid='" + ssid + '\'' +
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
