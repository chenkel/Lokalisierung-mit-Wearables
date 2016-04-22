package project.context.localization.wear;

import android.content.Context;
import android.content.res.Resources;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import project.context.localization.common.helper.PositionHelper;


/**
 * The WearableAdapter extends the {@link WearableListView.Adapter}.
 *
 * It create an Adapter to populate the List with an Array of Items.
 *
 * Also see: http://developer.android.com/training/wearables/ui/lists.html#adapter
 */
class WearableAdapter extends WearableListView.Adapter {
    static final int ITEM_LOCALIZATION = 0;
    static final int ITEM_CALCULATE = 9;
    static final int ITEM_DELETE = 10;

    private final ArrayList<Integer> mItems;
    private final LayoutInflater mInflater;
    private final Resources mRes;
    private final Context mContext;

    /**
     * Instantiates a new WearableAdapter.
     *
     * @param context the context of the application
     * @param items   the items to add given in by {@link MainWatchActivity}
     */
    public WearableAdapter(Context context, ArrayList<Integer> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
        mContext = context;
        mRes = context.getResources();
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, new LinearLayout(mContext), false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        TextView textView = (TextView) holder.itemView.findViewById(R.id.name);
        switch (position) {
            case ITEM_LOCALIZATION:
                textView.setText(R.string.menu_start_localization);
                break;
            case PositionHelper.ITEM_SCAN11:
            case PositionHelper.ITEM_SCAN12:
            case PositionHelper.ITEM_SCAN21:
            case PositionHelper.ITEM_SCAN22:
            case PositionHelper.ITEM_SCAN31:
            case PositionHelper.ITEM_SCAN32:
            case PositionHelper.ITEM_SCAN41:
            case PositionHelper.ITEM_SCAN42:
                String menuText = String.format(Locale.getDefault(), mRes.getString(R.string.menu_measure_place), PositionHelper.getMenuLabelForPosition(position));
                textView.setText(menuText);
                break;
            case ITEM_CALCULATE:
                textView.setText(R.string.menu_calculate_average);
                break;
            case ITEM_DELETE:
                textView.setText(R.string.menu_delete_all_measurements);
                break;
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            textView = (TextView) itemView.findViewById(R.id.name);
        }
    }


}
