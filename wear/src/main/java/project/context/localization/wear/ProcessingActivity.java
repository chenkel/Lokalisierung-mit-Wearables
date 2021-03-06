package project.context.localization.wear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import project.context.localization.common.superclasses.AverageMeasuresClass;
import project.context.localization.common.superclasses.MeasureClass;

/**
 * The Processing activity is a reusable Activity for displaying the
 * progress of measuring and calculating the average measurements.
 *
 * Since the implementation of {@link ProgressDialog} on Android Wear is
 * poorly implemented (e. g. no swipe-left-to-cancel listener built in)
 * this class serves as a custom Progress Dialog for Android Wear.
 *
 * Once again, UI interfacing methods of {@link MeasureClass} or {@link AverageMeasuresClass}
 * are overridden in this context and custom behaviour is added.
 */
public class ProcessingActivity extends Activity {
    /*private static final String TAG = ProcessingActivity.class.getSimpleName();*/

    private boolean allowDestroy = false;
    private MeasureClass mMeasureClass;

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
                    mMeasureClass = new MeasureClass(this) {
                        @Override
                        protected void showMeasureProgress() {
                            descriptionTextView.setText(R.string.processing_measuring_description);
                            String processingString = String.format(Locale.getDefault(), getResources().getString(R.string.processing_progress), 0, progressBar.getMax());
                            processingTextView.setText(processingString);
                            progressBar.setMax(mMeasureClass.scanCountMax);
                        }

                        @Override
                        protected void showMeasuresSaveProgress() {
                            descriptionTextView.setText(R.string.processing_measurements_save);
                            progressBar.setMax(wiFiMeasurements.size());
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
                    mMeasureClass.setPlaceString(placeId);
                    mMeasureClass.measureWiFi();

                    break;
                case "average":
                    AverageMeasuresClass mAverageMeasuresClass = new AverageMeasuresClass(this) {
                        @Override
                        protected void showCalculationProgressOutput() {
                            descriptionTextView.setText(R.string.processing_average_description);

                        }

                        @Override
                        protected void setMaxProgressOutput(int maxProgress) {
                            progressBar.setMax(maxProgress);
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

                    mAverageMeasuresClass.calculateAverageMeasures();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}
