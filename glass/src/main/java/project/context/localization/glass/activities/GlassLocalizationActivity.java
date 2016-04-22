package project.context.localization.glass.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

import project.context.localization.common.superclasses.LocalizationClass;
import project.context.localization.glass.card.CardAdapter;


/**
 * The Glass Localization activity uses the {@link LocalizationClass} to determine
 * the users location by WiFi Fingerprinting and Bluetooth Beacons in his vicinity.
 */
public class GlassLocalizationActivity extends Activity implements BeaconConsumer {
    private static final String TAG = GlassLocalizationActivity.class.getSimpleName();

    private static final int CARD_STATUS = 0;

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;
    private CardBuilder mScanCard;

    private LocalizationClass mLocalizationClass;

    private BeaconManager beaconManager;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();

        initializeBeaconManager();

        /* Instantiate a LocalizationClass and override necessary UI interfaces */
        mLocalizationClass = new LocalizationClass(this) {
            @Override
            protected void updateLocalizationProgressUI(String foundPlaceId, String placeDescription) {
                Log.i(TAG, "foundPlaceId: " + foundPlaceId);
                mScanCard.setText(placeDescription);
                mScanCard.setFootnote("Ort: " + foundPlaceId);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            protected void notifyLocationChange(String priorPlaceId, String foundPlaceId) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.SUCCESS);
            }

            @Override
            protected void showLocalizationProgressOutput() {
            }
        };
        mLocalizationClass.startLocalization();
    }

    /**
     * Create Localization progress card.
     * @param context The application context
     * @return list with Cards
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<>();
        mScanCard = new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Scanne die Umgebung")
                .setFootnote("einen Moment noch...");
        cards.add(CARD_STATUS, mScanCard);
        return cards;
    }

    /**
     * Initialize the BeaconManager to receive new beacons in range.
     */
    private void initializeBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
    }

    /**
     * Set the range notifier in onBeaconServiceConnect and start looking for beacons.
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(mLocalizationClass.rangeNotifier);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingWatchId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        beaconManager.unbind(this);
        mCardScroller.deactivate();
        mLocalizationClass.stopLocalization();
        super.onPause();
    }

    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.DISALLOWED;
                /* Play sound. */
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }
}
