package project.context.localization.mobile;

import android.app.Activity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import project.context.localization.common.helper.WiFiMeasurement;
import project.context.localization.common.superclasses.MeasureClass;

/**
 * The PhoneMeasure class implements methods to manipulate the UI accordingly offered by the {@link MeasureClass}..
 */
public class PhoneMeasureClass extends MeasureClass {
    /**
     * Instantiates a new PhoneMeasure class.
     *
     * @param activity the activity {@link MainPhoneActivity}
     */
    public PhoneMeasureClass(Activity activity) {
        super(activity);
    }

    @Override
    protected void outputDebugInfos(List<WiFiMeasurement> wiFiMeasurementList) {
                /* Sorting of WiFiMeasurement */
        Comparator<WiFiMeasurement> wlanComparator = new Comparator<WiFiMeasurement>() {
            @Override
            public int compare(WiFiMeasurement lhs, WiFiMeasurement rhs) {
                return (lhs.getRssi() > rhs.getRssi() ? -1 : (lhs.getRssi() == rhs.getRssi() ? 0 : 1));
            }
        };

        Collections.sort(wiFiMeasurementList, wlanComparator);

                /* only show last measurement in list */
        for (WiFiMeasurement ap : wiFiMeasurementList) {
            String helperString = "SSID: " + ap.getSsid()
                    + "\nRSSI: " + ap.getRssi()
                    + "\nBSSI: " + ap.getBssi();
            ((MainPhoneActivity) activity).debugOutputList.add(helperString);
        }
                /* Update the table */
        ((MainPhoneActivity) activity).wifiArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateMeasurementsCount() {
        this.setPlaceString(((MainPhoneActivity) activity).editPlaceId.getText().toString());
        //* Sanity checks *//*
        if (!(placeString.isEmpty())) {
            ((MainPhoneActivity) activity).textViewMeasuresCount.setText(db.getNumberOfDistinctMeasurementsByBssiForPlace(placeString));
        }
    }
}
