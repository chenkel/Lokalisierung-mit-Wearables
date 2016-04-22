package project.context.localization.glass.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

import project.context.localization.glass.R;
import project.context.localization.glass.card.CardAdapter;
import project.context.localization.glass.qr.barcode.scan.CaptureActivity;

/**
 * The Main {@link Activity} showing the start menu to choose between the localization methods.
 */
public class MainGlassActivity extends Activity {

    private static final String TAG = MainGlassActivity.class.getSimpleName();

    // Index of api demo cards.
    // Visible for testing.
    private static final int CARD_QR = 0;
    private static final int CARD_WIFI_BLE = 1;

    private CardScrollView mCardScroller;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        super.onCreate(bundle);

        CardScrollAdapter mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
    }

    /**
     * Creates list of cards serving as the menu for the user.
     <p>
     * First, the card for the QR code method.
     * Second, the card for the combined localization method (WiFi + BLE Beacons).
     *
     * @param context The application context
     * @return list with Cards
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<>();

        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setText(getString(R.string.qr_code_localization))
                .setIcon(R.drawable.ic_qr_code)
                .setFootnote(R.string.qr_code_footnote));

        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setText(getString(R.string.wifi_ble_localization))
                .setIcon(R.drawable.ic_wifi_ble)
                .setFootnote(R.string.wifi_ble_footnote));

        return cards;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    /**
     * Different type of activities can be shown, when tapped on a card.
     */
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.TAP;
                switch (position) {
                    case CARD_QR:
                        startActivity(new Intent(MainGlassActivity.this, CaptureActivity.class));
                        break;
                    case CARD_WIFI_BLE:
                        startActivity(new Intent(MainGlassActivity.this, WiFiBleActivity.class));
                        break;

                    default:
                        soundEffect = Sounds.ERROR;
                        Log.d(TAG, "Don't show anything");
                }

                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }
}
