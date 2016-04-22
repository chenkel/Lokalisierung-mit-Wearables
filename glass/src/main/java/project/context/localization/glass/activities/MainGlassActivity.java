package project.context.localization.glass.activities;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import project.context.localization.glass.R;
import project.context.localization.glass.card.CardAdapter;
import project.context.localization.glass.qr.barcode.scan.CaptureActivity;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
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
     * Create list of API demo cards.
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

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode == 0) {
            List<String> results = intent.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.w(TAG, spokenText);
            // Do something with spokenText.
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.w(TAG, spokenText);
            // Do something with spokenText.
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
