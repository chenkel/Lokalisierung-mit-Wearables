package smartwatch.context.project;

import android.os.Bundle;

import smartwatch.context.common.superclasses.CommonActivity;

public class LocalizationActivity extends CommonActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocalization();
    }

    @Override
    protected void onPause() {
        stopScanningAndCloseProgressDialog();
        super.onPause();
    }
}
