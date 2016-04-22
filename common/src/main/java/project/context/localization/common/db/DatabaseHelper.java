package project.context.localization.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * The DatabaseHelper provides a management class for the SQLite Database
 * to store measurement information (bssi, ssid, rssi) and averages for
 * WiFi access points at a specific place id.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
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
    private static DatabaseHelper sInstance;
    private SQLiteDatabase database = null;

    /**
     * Constructor is private to prevent direct instantiation.
     * Make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    /**
     * Gets DB instance.
     *
     * @param context the application context
     * @return the DB instance
     */
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
     * Inserts new records in average DB.
     *
     * @param placeId the place id
     * @param bssi    the AP's bssi
     * @param ssid    the AP's ssid
     * @param rssi    the average rssi of the AP
     * @return long of affected row id, returns -1 when failed.
     */
    public long addAverageRecords(String placeId, String bssi, String ssid, double rssi) {
        ContentValues values = new ContentValues();

        values.put(A_PLACE, placeId);
        values.put(A_BSSI, bssi);
        values.put(A_SSID, ssid);
        values.put(A_RSSI, rssi);

        return database.insert(A_TABLE, null, values);
    }

    /**
     * Gets average rssi by place.
     *
     * @param placeId the place id to filter the averages DB
     * @return Cursor linking to the average rssi for the queried place
     */
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

    /**
     * Delete all averages entries in DB.
     */
    public void deleteAverages() {
        database.delete(A_TABLE, null, null);
    }

    /**
     * Insert new measurements record.
     *
     * @param bssi    the new AP's bssi
     * @param ssid    the new AP's ssid
     * @param rssi    the new AP's scanned rssi
     * @param placeId the new AP's place id
     * @return long of affected row id, returns -1 when failed.
     */
    public long addMeasurementsRecords(String bssi, String ssid, Integer rssi, String placeId) {
        ContentValues values = new ContentValues();

        values.put(M_BSSI, bssi);
        values.put(M_SSID, ssid);
        values.put(M_RSSI, rssi);
        values.put(M_PLACE, placeId);

        return database.insert(M_TABLE, null, values);
    }

    /**
     * Gets all distinct places from measurements.
     *
     * @return Cursor holding all distinct places from measurements
     */
    public Cursor getAllDistinctPlacesFromMeasurements() {
        String queryString =
                "SELECT DISTINCT " + M_PLACE + " FROM " + M_TABLE;
        return database.rawQuery(queryString, null); // iterate to get each value.
    }


    /**
     * Gets measurements rssi average grouped by bssi and place Id.
     *
     * @return Cursor holding all measurements' placeId, BSSI, SSID and the average RSSI
     */
    public Cursor getMeasurementsRssiAvgByBssiAndPlace() {
        String queryString =
                "SELECT " + M_PLACE + ", " + M_BSSI + "," + M_SSID + ", AVG(" + M_RSSI + ") AS avgrssi " +
                        "FROM " + M_TABLE + " " +
                        "GROUP BY " + M_BSSI + "," + M_PLACE + " " +
                        "ORDER BY avgrssi DESC";
        return database.rawQuery(queryString, null); // iterate to get each value.
    }

    /**
     * Gets number of measurements with distinct bssis and total number of measurements for place.
     *
     * @param place the place id to filter the measurements table.
     * @return String with the number of distinct measurements of bssis and total measurements for place
     */
    public String getNumberOfDistinctMeasurementsByBssiForPlace(String place) {
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

    /**
     * Delete all measurements at a specific place id.
     *
     * @param placeIdString the place id
     */
    public void deleteMeasurementForPlaceId(String placeIdString) {
        database.delete(M_TABLE, M_PLACE + " = ?", new String[]{placeIdString});
    }

    /**
     * Delete all measurements.
     */
    public void deleteAllMeasurements() {
        database.delete(M_TABLE, null, null);
    }

    /**
     * Creates the Measurement and Average DB.
     *
     * @param db the sqlite db
     */
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

    /**
     * Drops the old DB and creates a new one.
     * @param db the db to be deleted and created again
     * @param oldVersion number of old db revision
     * @param newVersion number of new db revision
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Upgrading DB");
        Log.v(TAG, "Dropping" + A_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + A_TABLE);
        Log.v(TAG, "Dropping" + M_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + M_TABLE);
        onCreate(db);
    }

    /**
     * This method is needed for the DBManager in the Mobile module.
     * Also see <a href="https://github.com/sanathp/DatabaseManager_For_Android">
     * Database Manager for Android on GitHub</a>.
     *
     * @param Query interfacing argument for queries to be executed by DBManager.
     * @return an array list of cursor to save two cursors one has results from the query.
     */
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