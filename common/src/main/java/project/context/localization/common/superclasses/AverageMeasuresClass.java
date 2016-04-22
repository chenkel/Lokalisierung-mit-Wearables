package project.context.localization.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;


/**
 * The Average measures class calculates all the measurements RSSI averages
 * and stores them in the averages db.
 */
public class AverageMeasuresClass extends CommonClass {

    private static final String TAG = AverageMeasuresClass.class.getSimpleName();

    /**
     * Instantiates a new Average measures class.
     *
     * @param activity a reference to the instantiating activity to access its UI elements
     */
    public AverageMeasuresClass(Activity activity) {
        super(activity);
    }

    /**
     * Show calculation progress output.
     */
    protected void showCalculationProgressOutput(){
        progress = new ProgressDialog(getActivity());

        progress.setTitle("Berechnung der durchschn. Signalstärke...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }


    /**
     * Initiates the average calculation process.
     *
     * This method gets called by the activities.
     * The async task DoCalculationTask gets executed.
     */
    public void calculateAverageMeasures() {
        new AverageMeasuresClass.DoCalculationTask().execute();
    }

    /**
     * In the DoCalculationTask class an async task, to calculate the average RSSIs, gets initiated.
     *
     * In a first step, all previously calculated averages in the db are getting deleted.
     * Next, a sql query calculates the average RSSI
     * grouped by BSSI and Place from the measurement table.
     * The result is stored in the averages database and feedback
     * regarding the progress of the calculation is given to the user.
     */
    private class DoCalculationTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            showCalculationProgressOutput();
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            int calculationsCount = 0;
            try {

                /* Clear previous averages */
                db.deleteAverages();

                /* Create list that contains average of all BSSIs for all places */
                Cursor bssiCursor = db.getMeasurementsRssiAvgByBssiAndPlace();
                setMaxProgressOutput(bssiCursor.getCount());


                /*Cursor has placeId, bssi, ssid, avgrssi*/
                for (bssiCursor.moveToFirst(); !bssiCursor.isAfterLast(); bssiCursor.moveToNext()) {

                    /* Escape early if cancel() is called */
                    if (isCancelled()) break;
                    long affectedRow = db.addAverageRecords(bssiCursor.getString(0), bssiCursor.getString(1),
                            bssiCursor.getString(2), bssiCursor.getDouble(3));
                    if (affectedRow == -1){
                        Log.e(TAG, "Error inserting average Records");
                    }
                    calculationsCount = calculationsCount + 1;
                    publishProgress(calculationsCount);
                }

                bssiCursor.close();

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return calculationsCount;
        }

        protected void onProgressUpdate(Integer... calcProgress) {
            updateProgressOutput(calcProgress[0]);
        }

        protected void onPostExecute(Integer result) {
            hideProgressOutput();
        }
    }
}
