package project.context.localization.glass.qr.qrlens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import project.context.localization.glass.R;
import project.context.localization.glass.qr.barcode.scan.CaptureActivity;

/**
 * The Main activity of QRLens.
 *
 * <p>The code was imported from the following repository</p> https://github.com/jaxbot/glass-qrlens.
 *
 * <p>Some changes that were made will be explained in the following section:</p>
 * <ul>
 *      <li>
 *          In -- {@link CaptureActivity#onResume()} --
 *          <p>Time out of QR Code Scanner extended</p>
 *          <p>from 15 seconds to 60 seconds</p>
 *
 *     </li>
 *     <li>
 *         In -- {@link CaptureActivity#handleDecode(Result, Bitmap, float)} --
 *         <p>Play Beep sound every time</p>
 *     </li>
 *     <li>
 *         In -- {@link CaptureActivity#handleDecodeInternally(Result, Bitmap)} --
 *         <p>Does not cancel timer.</p>
 *         <p>Gets Text value from Qr Code and lookup location description by place id</p>
 *         <p>Resets SurfaceView by calling onPause(),</p>
 *         <p>re-initialising the camera and calling onResume()</p>
 *     </li>
 *     <li>
 *         In -- {@link project.context.localization.glass.qr.barcode.scan.ui.ViewfinderView#ViewfinderView(Context, AttributeSet)} --
 *         <p>Added LinearLayout with textView</p>
 *     </li>
 *     <li>
 *         In -- {@link project.context.localization.glass.qr.barcode.scan.ui.ViewfinderView#onDraw(Canvas)} --
 *         <p>Display resultText in TextView, adjust textView width and add it to the layout</p>
 *     </li>
 *</ul>
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<CardBuilder> mCards;

    private boolean mNeedsReadMore;
    private boolean invalid = false;

    private String mCardData;

    private Context context;

    private boolean allowDestroy = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.setResult(RESULT_CANCELED);

        context = this;
        invalid = false;

        Intent data = getIntent();
        Bundle res = data.getExtras();

        String qrtype = res.getString("qr_type");
        String qrdata = res.getString("qr_data");

        Log.w(TAG, qrtype);
        Log.w(TAG, qrdata);

        if (qrtype.equals("-1")) {
            showError(getString(R.string.unable_to_read));
            createView();
            allowDestroy = true;
            return;
        }
        if (qrtype.equals("-2")) {
            showError(getString(R.string.camera_error));
            createView();
            allowDestroy = true;
            return;
        }
        if (qrtype.equals("URI")) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrdata));
                startActivity(browserIntent);

                finish();
            } catch (Exception e) {
                showError(getString(R.string.unable_to_read));
                createView();
                allowDestroy = true;
            }
        } else {

            createCards(qrdata);
            createView();

            allowDestroy = true;
        }
	}

    private void showPagination()
    {
        allowDestroy = false;
        Intent intent = new Intent(this, ReadMoreActivity.class);
        intent.putExtra("qr_data", mCardData);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (allowDestroy) {
            this.setResult(RESULT_OK);
            finish();
        }
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 3) {
			if (resultCode == RESULT_OK) {
                finish();
            }
		}
	}

    private void createView() {
        CardScrollView mCardScrollView = new CardScrollView(this);
        MyCardScrollAdapter mAdapter = new MyCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.TAP);
                if (invalid)
                    finish();
                else
                    openOptionsMenu();
            }
        });
        setContentView(mCardScrollView);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if (mNeedsReadMore)
            inflater.inflate(R.menu.readmore, menu);
        else
            inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menu_item_1:
                showPagination();
                return true;
            case R.id.menu_item_2:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showError(String error) {
        mCards = new ArrayList<>();
        mCards.add(new CardBuilder(this, CardBuilder.Layout.ALERT)
                        .setIcon(R.drawable.ic_alert)
                        .setText(error)
                        .setFootnote(R.string.tap_to_try_again)
        );
        invalid = true;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.ERROR);
    }

    private void createCards(String data) {
        mCardData = data;
        mCards = new ArrayList<>();

        if (data.length() > 200 || data.split("\n").length > 7)
            mNeedsReadMore = true;

        String FOOTER = "QR text content";
        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
            .setText(data)
            .setFootnote(FOOTER)
        );

    }

    private class MyCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }
}

