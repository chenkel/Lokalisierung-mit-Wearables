package smartwatch.context.project.activities;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import smartwatch.context.common.helper.DataHelper;
import smartwatch.context.project.R;
import smartwatch.context.project.custom.MyAdapter;

public class BleActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private TextView sensorText;

    private float[] mGravity;
    private float[] mGeomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorText = (TextView) findViewById(R.id.magneticFieldData);

        DataHelper[] datenArray = new DataHelper[5];
        datenArray[0] = new DataHelper("01:ab", 50);
        datenArray[1] = new DataHelper("02:bb", 10);
        datenArray[2] = new DataHelper("03:cb", 20);
        datenArray[3] = new DataHelper("04:db", 40);
        datenArray[4] = new DataHelper("05:eb", 25);

        ArrayList<DataHelper> datenList = new ArrayList<>(Arrays.asList(datenArray));


        ListView theListView = (ListView) findViewById(R.id.listViewTest);
        MyAdapter namensAdapter = new MyAdapter(this, 0, datenList);
        theListView.setAdapter(namensAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values.clone();
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                float azimuthInDegress = ((float) Math.toDegrees(orientation[0]) + 360) % 360;
                String sensorTextString = azimuthInDegress + " ";
                sensorText.setText(sensorTextString);
            }
        }
    }

}
