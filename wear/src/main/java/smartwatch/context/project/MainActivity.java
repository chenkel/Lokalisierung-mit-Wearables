package smartwatch.context.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import smartwatch.context.common.superclasses.LocalizationActivity;
import smartwatch.context.common.superclasses.CommonActivity;

public class MainActivity extends CommonActivity {

    // Handle our Wearable List's click events
    private final WearableListView.ClickListener mClickListener =
            new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    int clickedMenu = viewHolder.getLayoutPosition();
                    switch (clickedMenu) {
                        case 0:
                            startActivity(new Intent(MainActivity.this, LocalizationActivity.class));
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            placeIdString = String.valueOf(clickedMenu);
//                            deleteAllMeasurementsForPlace();
                            scanWlan();
                            break;
                        case 6:
                            new DoCalculationTask().execute();
                            break;
                        case 7:
                            /* todo: call bluetooth scan! */
                            break;
                        case 8:
                            deleteAllMeasurements();
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
    private TextView mHeader;
    // The following code ensures that the title scrolls as the user scrolls up
    // or down the list
    private final WearableListView.OnScrollListener mOnScrollListener =
            new WearableListView.OnScrollListener() {
                @Override
                public void onScroll(int i) {
                    // Only scroll the title up from its original base position
                    // and not down.
                    /*if (i > 0) {
                        mHeader.setY(mHeader.getY() - i);
                    }*/
                }

                @Override
                public void onAbsoluteScrollChange(int i) {
                    /* Deprecated */
                }

                @Override
                public void onScrollStateChanged(int i) {
                    // Placeholder
                }

                @Override
                public void onCentralPositionChanged(int i) {
                    // Placeholder
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mIcons.add(R.drawable.ic_action_select_all);
        mIcons.add(R.drawable.ic_action_user);
        mIcons.add(R.drawable.ic_action_delete);


        // This is our list header
        mHeader = (TextView) findViewById(R.id.header);

        WearableListView wearableListView =
                (WearableListView) findViewById(R.id.wearable_List);
        wearableListView.setAdapter(new WearableAdapter(this, mIcons));
        wearableListView.setClickListener(mClickListener);
        wearableListView.addOnScrollListener(mOnScrollListener);


    }
}
