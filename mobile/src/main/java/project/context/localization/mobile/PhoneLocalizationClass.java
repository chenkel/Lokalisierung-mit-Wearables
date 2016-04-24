package project.context.localization.mobile;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

import project.context.localization.common.superclasses.LocalizationClass;

/**
 * The PhoneLocalization class implements methods to manipulate the UI accordingly offered by the {@link LocalizationClass}.
 */
public class PhoneLocalizationClass extends LocalizationClass {
    private final Vibrator v;

    /**
     * Instantiates a new PhoneLocalization class.
     *
     * @param activity the activity {@link MainPhoneActivity}
     */
    public PhoneLocalizationClass(Activity activity) {
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
