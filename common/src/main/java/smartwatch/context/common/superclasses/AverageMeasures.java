package smartwatch.context.common.superclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;


public class AverageMeasures extends CommonClass {

    private static final String TAG = AverageMeasures.class.getSimpleName();

    public AverageMeasures(Activity activity) {
        super(activity);
    }

    protected void showCalculationProgressOutput(){
        progress = new ProgressDialog(getActivity());

        progress.setTitle("Berechnung der durchschn. Signalstärken...");
//        progress.setMessage("Durchschnittliche Signalstärke aller APs für verschiede Orte wird berechnet");


        /*progress.setMessage("Bitte warten Sie einen Moment...");*/
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    public void calculateAverageMeasures() {
        new AverageMeasures.DoCalculationTask().execute();
    }

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
                Cursor bssiCursor = db.getMeasurementsRssiAvgByBssi();
                setMaxProgressOutput(bssiCursor.getCount());


                /*Cursor has placeId, bssi, ssid, avgrssi*/
                for (bssiCursor.moveToFirst(); !bssiCursor.isAfterLast(); bssiCursor.moveToNext()) {

                    /* Escape early if cancel() is called */
                    if (isCancelled()) break;
                    db.createAverageRecords(bssiCursor.getString(0), bssiCursor.getString(1),
                            bssiCursor.getString(2), bssiCursor.getDouble(3));
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
