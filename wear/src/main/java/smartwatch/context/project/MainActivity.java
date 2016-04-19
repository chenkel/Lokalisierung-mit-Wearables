package smartwatch.context.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import smartwatch.context.common.superclasses.Measure;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Measure mMeasure;
    private TextView mHeader;

    // Handle our Wearable List's click events
    protected final WearableListView.ClickListener mClickListener =
            new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    int clickedMenu = viewHolder.getLayoutPosition();
                    // TODO: 15.04.16 case 1, 2, 3 should be replaced by constants
                    switch (clickedMenu) {
                        case WearableAdapter.ITEM_LOCALIZATION:
                            startActivity(new Intent(MainActivity.this, LocalizationActivity.class));
                            break;
                        case WearableAdapter.ITEM_SCAN1:
                        case WearableAdapter.ITEM_SCAN2:
                        case WearableAdapter.ITEM_SCAN3:
                        case WearableAdapter.ITEM_SCAN4:
                        case WearableAdapter.ITEM_SCAN5:
                        case WearableAdapter.ITEM_SCAN6:
                        case WearableAdapter.ITEM_SCAN7:
                        case WearableAdapter.ITEM_SCAN8:
                            Intent intent = new Intent(MainActivity.this, ProcessingActivity.class);
                            intent.putExtra("mode", "measure");
                            intent.putExtra("placeId", String.valueOf(clickedMenu));
                            startActivity(intent);
                            break;

                        case WearableAdapter.ITEM_CALCULATE:
                            intent = new Intent(MainActivity.this, ProcessingActivity.class);
                            intent.putExtra("mode", "average");
                            startActivity(intent);
                            break;
                        case WearableAdapter.ITEM_DELETE:
                            mMeasure.deleteAllMeasurements();
                            break;
                        default:
                            Toast.makeText(MainActivity.this,
                                    String.format("You selected item #%s",
                                            viewHolder.getLayoutPosition() + 1),
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }

                }

                @Override
                public void onTopEmptyRegionClick() {
                    Toast.makeText(MainActivity.this,
                            "Top empty area tapped", Toast.LENGTH_SHORT).show();
                }
            };


    // The following code ensures that the title scrolls as the user scrolls up
    // or down the list
    private final WearableListView.OnScrollListener mOnScrollListener =
            new WearableListView.OnScrollListener() {
                @Override
                public void onScroll(int i) {}

                @Override
                public void onAbsoluteScrollChange(int i) {
                    if (i >= 0) {
                        mHeader.setY(mHeader.getY() - i);
                    } else {
                        mHeader.setY(0.0F);
                    }
                }

                @Override
                public void onScrollStateChanged(int i) {}

                @Override
                public void onCentralPositionChanged(int i) {}
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Sample icons for the list
        ArrayList<Integer> mIcons = new ArrayList<>();
        mIcons.add(R.drawable.ic_action_locate);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_share);
        mIcons.add(R.drawable.ic_action_select_all);
        mIcons.add(R.drawable.ic_action_delete);


        // This is our list header
        mHeader = (TextView) findViewById(R.id.header);

        mMeasure = new Measure(this) {
            @Override
            public void updateMeasurementsCount() {}
        };

        WearableListView wearableListView =
                (WearableListView) findViewById(R.id.wearable_List);

        WearableAdapter wa = new WearableAdapter(this, mIcons);
        wearableListView.setAdapter(wa);
        wearableListView.setClickListener(mClickListener);
        wearableListView.addOnScrollListener(mOnScrollListener);
    }


}
