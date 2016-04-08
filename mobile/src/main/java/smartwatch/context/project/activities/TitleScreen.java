package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import smartwatch.context.project.R;
import smartwatch.context.project.helper.DBManager;


public class TitleScreen extends Activity {

    //String text = "Das ist der Intent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        addListenerWlan();
        addListenerBle();

        addListenerQrcode();
        addListenerGraphic();
        addListenerDBManager();
    }

    private void addListenerWlan() {
        final Button wlanswitchact = (Button) findViewById(R.id.wlan);
        wlanswitchact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), WlanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addListenerBle() {
        final Button bleswitchact = (Button) findViewById(R.id.ble);
        bleswitchact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), BleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addListenerQrcode() {
        final Button qrcodeswitchact = (Button) findViewById(R.id.qr_code);
        qrcodeswitchact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), QrcodeActivity.class);
                startActivity(intent);

            }
        });
    }

    private void addListenerGraphic() {
        final Button graphicswitchact = (Button) findViewById(R.id.localization_graphic);
        graphicswitchact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GraphicActivity.class);
                startActivity(intent);

            }
        });
    }


    private void addListenerDBManager() {
        final Button dbManagerButton = (Button) findViewById(R.id.db_manager_averages);
        dbManagerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent dbManager = new Intent(view.getContext(), DBManager.class);
                startActivity(dbManager);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_title_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
