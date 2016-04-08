package smartwatch.context.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper sInstance;

    private SQLiteDatabase database = null;

    private static final String DATABASE_NAME = "LocalizationDB";

    // DB version
    private static final int DATABASE_VERSION = 10;

    /* Measurements Table */
    private final static String M_TABLE = "Measurements"; // name of table
    private final static String M_BSSI = "bssi";  // MAC address of Access point
    private final static String M_SSID = "ssid";  // SSID of Access point
    private final static String M_RSSI = "rssi";  // Signal Strength of AP
    private final static String M_PLACE = "placeId";  // id to locate place

    /* Averages Table */
    private static final String A_TABLE = "WlanAverages"; // name of table
    private static final String A_BSSI = "bssi";  // MAC address of Access point
    private static final String A_SSID = "ssid";  // SSID of Access point
    private static final String A_RSSI = "rssi";  // Signal Strength of AP
    private static final String A_PLACE = "placeId";  // id to locate place


    // Database creation sql statement
    private static final String M_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + M_TABLE +
            "(_id INTEGER PRIMARY KEY , " +
            M_BSSI + " TEXT NOT NULL , " +
            M_SSID + " TEXT , " +
            M_RSSI + " REAL NOT NULL , " +
            M_PLACE + " TEXT NOT NULL );";

    private static final String A_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + A_TABLE +
            "( " + M_BSSI + " TEXT NOT NULL, " +
            A_SSID + " TEXT, " +
            A_RSSI + " REAL NOT NULL, " +
            A_PLACE + " TEXT NOT NULL, " +
            "PRIMARY KEY (" + A_BSSI + "," + A_PLACE + ") );";

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }


    public long createAverageRecords(String placeId, String bssi, String ssid, double rssi) {
        ContentValues values = new ContentValues();

        values.put(A_PLACE, placeId);
        values.put(A_BSSI, bssi);
        values.put(A_SSID, ssid);
        values.put(A_RSSI, rssi);

        return database.insert(A_TABLE, null, values);
    }

    public Cursor getAverageRssiByPlace(String placeId) {
        String queryString =
                "SELECT DISTINCT " + A_BSSI + "," + A_RSSI + "," + A_SSID + " " +
                        "FROM " + A_TABLE + " " +
                        "WHERE " + A_PLACE + " = ? " + " " +
                        "ORDER BY " + A_PLACE + " DESC";
        String[] whereArgs = new String[]{
                placeId
        };
        return database.rawQuery(queryString, whereArgs); // iterate to get each value.
    }

    public void deleteAverages() {
        database.delete(A_TABLE, null, null);
    }

    public long createMeasurementsRecords(String bssi, String ssid, Integer rssi, String placeId) {
        ContentValues values = new ContentValues();

        values.put(M_BSSI, bssi);
        values.put(M_SSID, ssid);
        values.put(M_RSSI, rssi);
        values.put(M_PLACE, placeId);

        return database.insert(M_TABLE, null, values);
    }

    public Cursor getAllDistinctPlacesFromMeasurements() {
        String queryString =
                "SELECT DISTINCT " + M_PLACE + " FROM " + M_TABLE;
        return database.rawQuery(queryString, null); // iterate to get each value.
    }


    public Cursor getMeasurementsRssiAvgByBssi() {
        String queryString =
                "SELECT " + M_PLACE + ", " + M_BSSI + "," + M_SSID + ", AVG(" + M_RSSI + ") AS avgrssi " +
                        "FROM " + M_TABLE + " " +
                        "GROUP BY " + M_BSSI + "," + M_PLACE + " " +
                        "ORDER BY avgrssi DESC";
        return database.rawQuery(queryString, null); // iterate to get each value.
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


    public String getMeasurementsNumberOfBssisForPlace(String place) {
        if (place == null || place.isEmpty()) {
            Log.w(TAG, "Place is empty.");
            return "0";
        }
        String sFoundCount = "0";
        String queryString =
                "SELECT count(DISTINCT " + M_BSSI + "), count(" + M_BSSI + ") " +
                        "FROM " + M_TABLE + " " +
                        "WHERE " + M_PLACE + " = ?";
        String[] whereArgs = new String[]{
                place

        };
        Cursor mCursor = database.rawQuery(queryString, whereArgs);


        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                sFoundCount = "APs: " + String.valueOf(mCursor.getInt(0));
                sFoundCount = sFoundCount + " || Datensätze: " + String.valueOf(mCursor.getInt(1));
                if (mCursor.getInt(0) != 0) {
                    sFoundCount = sFoundCount + " || Messungen: " + String.valueOf(Math.round(mCursor.getFloat(1) / mCursor.getFloat(0)));
                }
            }
            mCursor.close();
        }

        return sFoundCount;
    }

    public void deleteMeasurementForPlaceId(String placeIdString) {
        database.delete(M_TABLE, M_PLACE + " = ?", new String[]{placeIdString});
    }

    public void deleteAllMeasurements() {
        database.delete(M_TABLE, null, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "Creating DB");
        db.beginTransaction();
        try {
            db.execSQL(A_DATABASE_CREATE);
            db.execSQL(M_DATABASE_CREATE);
            db.setTransactionSuccessful();
        } catch (SQLException sqlException) {
            Log.e(TAG, sqlException.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Upgrading DB");
        Log.v(TAG, "Dropping" + A_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + A_TABLE);
        Log.v(TAG, "Dropping" + M_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + M_TABLE);
        onCreate(db);
    }


    /* TODO: Only needed for DBManager, delete in the end */
    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(Query, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }
}