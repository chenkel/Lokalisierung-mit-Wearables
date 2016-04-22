package project.context.localization.glass.qr.qrlens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.AttributeSet;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.context.localization.glass.R;
import project.context.localization.glass.qr.barcode.scan.CaptureActivity;

/**
 * The ReadMoreActivity of QRLens.
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
public class ReadMoreActivity extends Activity {
    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    private MyCardScrollAdapter mAdapter;

    private String mCardData;

    private Context context;

    private boolean allowDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setResult(RESULT_CANCELED);

        context = this;

        Intent data = getIntent();
        Bundle res = data.getExtras();

        mCardData = res.getString("qr_data");

        mCardScrollView = new CardScrollView(this);

        createCardsPaginated();
        createView();

        allowDestroy = true;
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

    private void createView() {
        mAdapter = new MyCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
            }
        });
        setContentView(mCardScrollView);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menu_item_2:
                this.setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int numNewlines(String str)
    {
        Matcher m = Pattern.compile("(\n)|(\r)|(\r\n)").matcher(str);
        int lines = 0;
        while (m.find())
        {
            lines ++;
        }
        return lines;
    }

    private void createCardsPaginated() {
        mCards = new ArrayList<>();

        String[] chunks = mCardData.split("\\b");

        int lines;
        String line;

        for (int i = 0; i < chunks.length; i++) {
            String hunk = "";
            line = "";
            lines = 0;
            for (; i < chunks.length; i++) {
                if ((line + chunks[i]).length() > 23) {
                    line = "";
                    lines++;
                }
                line += chunks[i];
                if (numNewlines(chunks[i]) > 0)
                {
                    line = "";
                    lines++;
                }
                if (lines > 6)
                {
                    i--;
                    break;
                }
                hunk += chunks[i];
            }
            if (hunk.substring(0, 2).equals("\r\n"))
                hunk = hunk.substring(2);
            if (hunk.substring(0, 1).equals(" ") || hunk.substring(0, 1).equals("\n") || hunk.substring(0, 1).equals("\r"))
                hunk = hunk.substring(1);
            mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED)
                            .setText(hunk)
            );
        }

        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();
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
