package project.context.localization.common.helper;


public class WlanMeasurement {

    private final String bssi;
    private final int rssi;
    private final String ssid;


    public WlanMeasurement(String bssi, int rssi, String ssid) {
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
        return "WlanMeasurement{" +
                "bssi='" + bssi + '\'' +
                ", rssi=" + rssi +
                ", ssid='" + ssid + '\'' +
                '}';
    }

    /*public int compare(Object o1, Object o2)
    {
        WlanMeasurement p1 = (WlanMeasurement)o1;
        WlanMeasurement p2 = (WlanMeasurement)o2;
        // if last names are the same compare first names
        if(p1.getBssi().equals(p2.getBssi()))
        {
            return p1.getFirstName().compareTo(p2.getFirstName());
        }
        return p1.getLastName().compareTo(p2.getLastName());

    }*/

}
