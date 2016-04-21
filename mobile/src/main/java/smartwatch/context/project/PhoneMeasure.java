package smartwatch.context.project;

import android.app.Activity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import smartwatch.context.common.helper.WlanMeasurements;
import smartwatch.context.common.superclasses.Measure;

public class PhoneMeasure extends Measure {
    public PhoneMeasure(Activity activity) {
        super(activity);
    }

    @Override
    protected void outputDebugInfos(List<WlanMeasurements> wlanMeasure) {
                /* Sorting of WlanMeasurements */
        Comparator<WlanMeasurements> wlanComparator = new Comparator<WlanMeasurements>() {
            @Override
            public int compare(WlanMeasurements lhs, WlanMeasurements rhs) {
                return (lhs.getRssi() > rhs.getRssi() ? -1 : (lhs.getRssi() == rhs.getRssi() ? 0 : 1));
            }
        };

        Collections.sort(wlanMeasure, wlanComparator);

                /* only show last measurement in list */
        for (WlanMeasurements ap : wlanMeasure) {
            String helperString = "SSID: " + ap.getSsid()
                    + "\nRSSI: " + ap.getRssi()
                    + "\nBSSI: " + ap.getBssi();
            outputList.add(helperString);
        }
                /* Update the table */
        ((MainPhoneActivity) activity).wifiArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateMeasurementsCount() {
        this.setPlaceIdString(((MainPhoneActivity) activity).editPlaceId.getText().toString());
        //* Sanity checks *//*
        if (!(placeIdString.isEmpty())) {
            ((MainPhoneActivity) activity).textViewMeasuresCount.setText(db.getMeasurementsNumberOfBssisForPlace(placeIdString));
        }
    }
}
