package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;

import java.util.ArrayList;
import java.util.List;

import smartwatch.context.project.card.CardAdapter;

/**
 * Created by chenkel on 07.04.16.
 */
public class GlassLocalizationActivity extends Activity {
    private static final String TAG = GlassLocalizationActivity.class.getSimpleName();

    // Index of api demo cards.
    // Visible for testing.
    static final int CARD_STATUS = 0;

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;
    private CardBuilder mScanCard;
    private Slider mSlider;
    private Slider.Indeterminate mIndeterminate;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
        mSlider = Slider.from(mCardScroller);
        mIndeterminate = mSlider.startIndeterminate();
    }

    /**
     * Create list of API demo cards.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        mScanCard = new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("scanning")
                .setFootnote("nothing found yet");
        cards.add(CARD_STATUS, mScanCard);
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

    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.DISALLOWED;
                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }


}
