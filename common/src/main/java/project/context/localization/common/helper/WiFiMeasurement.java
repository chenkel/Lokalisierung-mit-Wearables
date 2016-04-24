package project.context.localization.common.helper;


/**
 * The WiFi measurement class can be seen as a struct.
 *
 * It holds the three basic information about a WiFi Access Point.
 *
 * SSID is the Name of a Network
 * BSSI MAC address that identifies the Access Point
 * RSSI is a measurement of the power present in the received radio signal of the AP
 */
public class WiFiMeasurement {

    private final String bssi;
    private final int rssi;
    private final String ssid;


    /**
     * Instantiates a new Wi fi measurement.
     *
     * @param ssid the ssid is the Name of a Network
     * @param bssi the MAC address that identifies the Access Point
     * @param rssi the rssi is a measurement of the power present in the received radio signal of the AP
     */
    public WiFiMeasurement(String bssi, int rssi, String ssid) {
        this.bssi = bssi;
        this.rssi = rssi;
        this.ssid = ssid;
    }

    /**
     * Gets bssi.
     *
     * @return the bssi
     */
    public String getBssi() {
        return bssi;
    }

    /**
     * Gets rssi.
     *
     * @return the rssi
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Gets ssid.
     *
     * @return the ssid
     */
    public String getSsid() {
        return ssid;
    }

    @Override
    public String toString() {
        return "WiFiMeasurement{" +
                "bssi='" + bssi + '\'' +
                ", rssi=" + rssi +
                ", ssid='" + ssid + '\'' +
                '}';
    }
}
