package smartwatch.context.project;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

import smartwatch.context.common.superclasses.Localization;

public class PhoneLocalization extends Localization {
    private Vibrator v;

    public PhoneLocalization(Activity activity) {
        super(activity);
        v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void notifyLocationChange(String priorPlaceId, String foundPlaceId) {
        // Vibrate for 500 milliseconds if place changed
        v.vibrate(500);
    }

    @Override
    protected void outputDetailedPlaceInfoDebug(String output) {
        ((MainPhoneActivity) activity).textViewDebug.setText(output);
    }
}
