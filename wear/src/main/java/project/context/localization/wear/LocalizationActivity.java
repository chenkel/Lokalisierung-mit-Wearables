package project.context.localization.wear;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import project.context.localization.common.superclasses.LocalizationClass;

public class LocalizationActivity extends Activity implements BeaconConsumer {
    /* private static final String TAG = LocalizationActivity.class.getSimpleName(); */

    private BeaconManager beaconManager;

    private LocalizationClass mLocalizationClass;
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final TextView descriptionTextView = (TextView) findViewById(R.id.description);
        final TextView processingTextView = (TextView) findViewById(R.id.processing);
        processingTextView.setText(R.string.processing_localization_running);

        mLocalizationClass = new LocalizationClass(this) {
            @Override
            protected void notifyLocationChange(String priorPlaceId, String foundPlaceId) {
                v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
            }

            @Override
            protected void showLocalizationProgressOutput() {
            }

            @Override
            protected void updateLocalizationProgressUI(String foundPlaceId, String locationDescription) {
                descriptionTextView.setText(locationDescription);
            }
        };
        mLocalizationClass.startLocalization();

        initializeBeaconManager();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalizationClass.stopLocalization();
        beaconManager.unbind(this);
    }

    private void initializeBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(mLocalizationClass.rangeNotifier);

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingWatchId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}