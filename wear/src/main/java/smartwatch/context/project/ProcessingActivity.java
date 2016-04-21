package smartwatch.context.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import smartwatch.context.common.superclasses.AverageMeasures;
import smartwatch.context.common.superclasses.Measure;

public class ProcessingActivity extends Activity {
    /*private static final String TAG = ProcessingActivity.class.getSimpleName();*/

    boolean allowDestroy = false;
    private Measure mMeasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_processing);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final TextView descriptionTextView = (TextView) findViewById(R.id.description);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final TextView processingTextView = (TextView) findViewById(R.id.processing);

        processingTextView.setText(R.string.processing_measuring_default);

        Intent data = getIntent();
        Bundle res = data.getExtras();


        String mode = res.getString("mode");


        if (mode != null) {
            switch (mode) {
                case "measure":
                    mMeasure = new Measure(this) {
                        @Override
                        protected void showMeasureProgress() {
                            descriptionTextView.setText(R.string.processing_measuring_description);
                            String processingString = String.format(Locale.getDefault(), getResources().getString(R.string.menu_measure_place), mMeasure.scanCountMax);
                            processingTextView.setText(processingString);
                            progressBar.setMax(mMeasure.scanCountMax);
                        }

                        @Override
                        protected void showMeasuresSaveProgress() {
                            descriptionTextView.setText(R.string.processing_measurements_save);
                            progressBar.setMax(wlanMeasure.size());
                            allowDestroy = true;
                        }

                        @Override
                        public void updateMeasurementsCount() {
                        }

                        @Override
                        protected void updateProgressOutput(int iProgress) {
                            progressBar.setProgress(iProgress);
                            String processingString = String.format(Locale.getDefault(), getResources().getString(R.string.processing_progress), iProgress, progressBar.getMax());
                            processingTextView.setText(processingString);
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
                    AverageMeasures mAverageMeasures = new AverageMeasures(this) {
                        @Override
                        protected void showCalculationProgressOutput() {
                            descriptionTextView.setText(R.string.processing_average_description);

                        }

                        @Override
                        protected void setMaxProgressOutput(int count) {
                            progressBar.setMax(count);
                        }

                        @Override
                        protected void updateProgressOutput(int iProgress) {
                            progressBar.setProgress(iProgress);
                            String processingString = String.format(Locale.getDefault(), getResources().getString(R.string.processing_progress), iProgress, progressBar.getMax());
                            processingTextView.setText(processingString);
                        }

                        @Override
                        protected void hideProgressOutput() {
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
