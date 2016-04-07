package smartwatch.context.common.superclasses;

import android.os.Bundle;
import android.view.WindowManager;

public class LocalizationActivity extends CommonActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startLocalization();

    }

    @Override
    protected void showScanProgress() {

    }

    @Override
    protected void showLocalizationProgressDialog() {

    }

    @Override
    protected void notifyLocationChange() {

    }

    @Override
    protected void updateLocalizationProgressUI(String foundPlaceId, String waypointDescription) {

    }



    @Override
    protected void outputDetailedPlaceInfoDebug(String output) {

    }

    @Override
    protected void updateMeasurementsCount() {

    }

    @Override
    protected void outputDebugInfos() {

    }

    @Override
    protected void showScansSaveProgress() {

    }

    @Override
    protected void setMaxProgressOutput(int count) {

    }

    @Override
    protected void showCalculationProgressOutput() {

    }

    @Override
    protected void updateProgressOutput(int scanCount) {

    }

    @Override
    protected void hideProgressOutput() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopScanningAndCloseProgressDialog();
        super.onPause();
    }
}
