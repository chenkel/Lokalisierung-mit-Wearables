package smartwatch.context.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WlanAveragesDBAccess {
    // --Commented out by Inspection (04.04.16, 15:26):private static final String TAG = "WlanAveragesDBAccess";

    private final static String M_TABLE = "WlanAverages"; // name of table
    private final static String M_BSSI = "bssi";  // MAC address of Access point
    private final static String M_SSID = "ssid";  // SSID of Access point
    private final static String M_RSSI = "rssi";  // Signal Strength of AP
    private final static String M_PLACE = "placeId";  // id to locate place
    private final SQLiteDatabase database;


    public WlanAveragesDBAccess(Context context) {
        WlanAveragesDBCreation dbHelper = new WlanAveragesDBCreation(context);
        database = dbHelper.getWritableDatabase();
    }


    public long createRecords(String placeId, String bssi, String ssid, double rssi) {
        ContentValues values = new ContentValues();

        values.put(M_PLACE, placeId);
        values.put(M_BSSI, bssi);
        values.put(M_SSID, ssid);
        values.put(M_RSSI, rssi);

        return database.insert(M_TABLE, null, values);
    }

    public Cursor getRssiByPlace(String placeId) {
        String queryString =
                "SELECT DISTINCT bssi,rssi, ssid FROM WlanAverages " +
                        "WHERE placeId = ? " +
                        "ORDER BY rssi DESC";
        String[] whereArgs = new String[]{
                placeId
        };
        return database.rawQuery(queryString, whereArgs); // iterate to get each value.
    }

// --Commented out by Inspection START (04.04.16, 15:26):
//    public Cursor getAll() {
//        String[] cols = new String[]{M_BSSI, M_SSID, M_RSSI, M_PLACE,};
//        Cursor mCursor = database.query(true, M_TABLE, cols, null
//                , null, null, null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor; // iterate to get each value.
//    }
// --Commented out by Inspection STOP (04.04.16, 15:26)

    public void deleteMeasurements() {
        database.delete(M_TABLE, null, null);
    }
}