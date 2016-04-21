package smartwatch.context.project;

import android.app.Activity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import smartwatch.context.common.helper.WlanMeasurement;
import smartwatch.context.common.superclasses.MeasureClass;

public class PhoneMeasureClass extends MeasureClass {
    public PhoneMeasureClass(Activity activity) {
        super(activity);
    }

    @Override
    protected void outputDebugInfos(List<WlanMeasurement> wlanMeasure) {
                /* Sorting of WlanMeasurement */
        Comparator<WlanMeasurement> wlanComparator = new Comparator<WlanMeasurement>() {
            @Override
            public int compare(WlanMeasurement lhs, WlanMeasurement rhs) {
                return (lhs.getRssi() > rhs.getRssi() ? -1 : (lhs.getRssi() == rhs.getRssi() ? 0 : 1));
            }
        };

        Collections.sort(wlanMeasure, wlanComparator);

                /* only show last measurement in list */
        for (WlanMeasurement ap : wlanMeasure) {
            String helperString = "SSID: " + ap.getSsid()
                    + "\nRSSI: " + ap.getRssi()
                    + "\nBSSI: " + ap.getBssi();
            ((MainPhoneActivity) activity).outputList.add(helperString);
        }
                /* Update the table */
        ((MainPhoneActivity) activity).wifiArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateMeasurementsCount() {
        this.setPlaceIdString(((MainPhoneActivity) activity).editPlaceId.getText().toString());
        //* Sanity checks *//*
        if (!(placeIdString.isEmpty())) {
            ((MainPhoneActivity) activity).textViewMeasuresCount.setText(db.getMeasurementsNumberOfDistinctBssisForPlace(placeIdString));
        }
    }
}
