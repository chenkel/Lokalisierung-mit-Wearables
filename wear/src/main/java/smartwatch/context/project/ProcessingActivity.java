package smartwatch.context.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import smartwatch.context.common.superclasses.AverageMeasures;
import smartwatch.context.common.superclasses.Measure;

public class ProcessingActivity extends Activity {
    private static final String TAG = ProcessingActivity.class.getSimpleName();

    private Context context;
    private boolean invalid = false;

    boolean allowDestroy = false;
    private Measure mMeasure;
    private AverageMeasures mAverageMeasures;
    private int maxProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_localization);

        final TextView descriptionTextView = (TextView) findViewById(R.id.description);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final TextView processingTextView = (TextView) findViewById(R.id.processing);


        this.setResult(RESULT_CANCELED);

        context = this;
        invalid = false;

        Intent data = getIntent();
        Bundle res = data.getExtras();

        String mode = res.getString("mode");

        if (mode != null) {
            switch (mode) {
                case "measure":
                    mMeasure = new Measure(this) {


                        @Override
                        protected void showMeasureProgress() {
                            // TODO: 15.04.16 Strings to resources
                            descriptionTextView.setText("Messung der Signalstärken der WiFi-APs in der Umgebung läuft...");
                            processingTextView.setText("0/" + mMeasure.scanCountMax);
                            progressBar.setMax(mMeasure.scanCountMax);
                        }

                        @Override
                        protected void showMeasuresSaveProgress() {
                            descriptionTextView.setText("Alle Messdaten werden gespeichert...");
                            progressBar.setMax(wlanMeasure.size());
                        }

                        @Override
                        public void updateMeasurementsCount() {
                            allowDestroy = true;
                        }

                        @Override
                        protected void updateProgressOutput(int iProgress) {
                            progressBar.setProgress(iProgress);
                            processingTextView.setText(iProgress + "/" + progressBar.getMax());
                        }

                        @Override
                        protected void hideProgressOutput() {
                            if (allowDestroy) {
                                onPause();
                            }

                        }
                    };
                    String placeId = res.getString("placeId");
                    mMeasure.setPlaceIdString(placeId);
                    mMeasure.measureWlan();

                    break;
                case "average":
                    mAverageMeasures = new AverageMeasures(this) {
                        @Override
                        protected void showCalculationProgressOutput() {
                            descriptionTextView.setText("Durchschnittliche Signalstärken aller Router für die verschieden Messpunkte werden berechnet...");

                        }

                        @Override
                        protected void setMaxProgressOutput(int count) {
                            progressBar.setMax(count);
                        }

                        @Override
                        protected void updateProgressOutput(int iProgress) {
                            progressBar.setProgress(iProgress);
                            processingTextView.setText(iProgress + "/" + progressBar.getMax());
                        }

                        @Override
                        protected void hideProgressOutput() {
                            allowDestroy = true;
                            onPause();
                        }
                    };

                    mAverageMeasures.calculateAverageMeasures();

                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMeasure.stopScanningAndCloseProgressDialog();
        this.setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }


}
