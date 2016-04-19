package smartwatch.context.project;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;



class WearableAdapter extends WearableListView.Adapter {
    private final ArrayList<Integer> mItems;
    private final LayoutInflater mInflater;

    static final int ITEM_LOCALIZATION = 0;
    static final int ITEM_SCAN1 = 1;
    static final int ITEM_SCAN2 = 2;
    static final int ITEM_SCAN3 = 3;
    static final int ITEM_SCAN4 = 4;
    static final int ITEM_SCAN5 = 5;
    static final int ITEM_SCAN6 = 6;
    static final int ITEM_SCAN7 = 7;
    static final int ITEM_SCAN8 = 8;
    static final int ITEM_CALCULATE = 9;
    static final int ITEM_DELETE = 10;

    public WearableAdapter(Context context, ArrayList<Integer> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        CircledImageView circledView = itemViewHolder.mCircledImageView;
        circledView.setImageResource(mItems.get(position));
        TextView textView = itemViewHolder.mItemTextView;
        switch (position) {
            case ITEM_LOCALIZATION:
                textView.setText("Start");
                break;
            case ITEM_SCAN1:
            case ITEM_SCAN2:
            case ITEM_SCAN3:
            case ITEM_SCAN4:
            case ITEM_SCAN5:
            case ITEM_SCAN6:
            case ITEM_SCAN7:
            case ITEM_SCAN8:
                textView.setText("Ort " + position + " messen");
                break;
            case ITEM_CALCULATE:
                textView.setText("Berechne Durchschnitt");
                break;
            case ITEM_DELETE:
                textView.setText("Alle Messungen löschen");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private static class ItemViewHolder extends WearableListView.ViewHolder {
        private final CircledImageView mCircledImageView;
        private final TextView mItemTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mCircledImageView = (CircledImageView)
                    itemView.findViewById(R.id.circle);
            mItemTextView = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
