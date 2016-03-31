package smartwatch.context.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WlanMeasurementsDBAccess {
    private static final String TAG = "WlanMDBAccess";

    private SQLiteDatabase database;

    public final static String M_TABLE = "Measurements"; // name of table
    public final static String M_BSSI = "bssi";  // MAC address of Access point
    public final static String M_SSID = "ssid";  // SSID of Access point
    public final static String M_RSSI = "rssi";  // Signal Strength of AP
    public final static String M_PLACE = "placeId";  // id to locate place


    public WlanMeasurementsDBAccess(Context context) {
        WlanMeasurementsDBCreation dbHelper = new WlanMeasurementsDBCreation(context);
        database = dbHelper.getWritableDatabase();
    }


    public long createRecords(String bssi, String ssid, Integer rssi, String placeId) {
        ContentValues values = new ContentValues();

        values.put(M_BSSI, bssi);
        values.put(M_SSID, ssid);
        values.put(M_RSSI, rssi);
        values.put(M_PLACE, placeId);

        return database.insert(M_TABLE, null, values);
    }

    public Cursor getAll() {
        String[] cols = new String[]{M_BSSI, M_SSID, M_RSSI, M_PLACE,};
        Cursor mCursor = database.query(true, M_TABLE, cols, null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }

    public Cursor getAllPlaces() {
        String queryString =
                "SELECT DISTINCT placeId FROM Measurements ";
        Cursor mCursor = database.rawQuery(queryString, null);
        return mCursor; // iterate to get each value.
    }

    public Cursor getRssiAvgByBssi() {
        String queryString =
                "SELECT placeId, bssi, ssid, AVG(rssi) AS avgrssi " +
                        "FROM Measurements " +
                        "GROUP BY bssi, placeId ORDER BY avgrssi DESC";
        Cursor mCursor = database.rawQuery(queryString, null);
        return mCursor; // iterate to get each value.
    }


    /*public Cursor getEntries(String placeId, String bssi) {
        String queryString =
                "SELECT rssi FROM Measurements " +
                        "WHERE placeId = ? and bssi = ? ORDER BY rssi";
        String[] whereArgs = new String[] {
                placeId, bssi

        };
        Cursor mCursor = database.rawQuery(queryString, whereArgs);
        return mCursor; // iterate to get each value.
    }*/





    public String getNumberOfBssisForPlace(String place) {
        if (place == null || place.isEmpty()){
            Log.w(TAG, "Place is empty.");
            return "0";
        }
        String foundCount = "0";
        String queryString =
                "SELECT count(DISTINCT bssi), count(bssi) FROM Measurements " +
                        "WHERE placeId = ?";
        String[] whereArgs = new String[]{
                place

        };
        Cursor mCursor = database.rawQuery(queryString, whereArgs);

        if(mCursor != null) {
            if (mCursor.moveToFirst()) {
                foundCount = "APs: " + String.valueOf(mCursor.getInt(0));
                foundCount = foundCount + " || Datensätze: " + String.valueOf(mCursor.getInt(1));
                if (mCursor.getInt(0) != 0){
                    foundCount = foundCount + " || Messungen: " + String.valueOf(Math.round(mCursor.getFloat(1)/mCursor.getFloat(0)));
                }
            }
        }
        return foundCount;
    }

    public void deleteMeasurementForPlaceId(String placeIdString) {
        database.delete(M_TABLE, M_PLACE + " = ?", new String[] {placeIdString});
    }
}