package smartwatch.context.project.custom;

import smartwatch.context.common.helper.DataHelper;
import smartwatch.context.project.R;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jan on 23.03.16.
 */
public class MyAdapter extends ArrayAdapter<DataHelper> {
    private static final String TAG = "MyAdapter";
    private Activity activity;
    private ArrayList<DataHelper> data;
    private static LayoutInflater inflater = null;

    public MyAdapter(Activity activity, int textViewResourceId,ArrayList<DataHelper> data) {
        super(activity, textViewResourceId, data);
        try{
            this.activity = activity;
            this.data = data;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        catch (Exception e){}
    }


    public static class ViewHolder {
        public TextView display_name;
        public TextView display_number;

    }

    public int getCount() {
        return data.size();
    }

    public DataHelper getItem(DataHelper position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.row_layout, null);
                holder = new ViewHolder();

                holder.display_name = (TextView) vi.findViewById(R.id.textview1);
                holder.display_number = (TextView) vi.findViewById(R.id.textview2);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.display_name.setText(data.get(position).getBssi());
            holder.display_number.setText(String.valueOf(data.get(position).getRssi()));
            /*String.valueOf(data.get(position).getRssi())*/

        }catch (Exception e) {
            Log.i(TAG, e.toString());

        }
        return vi;
    }
}
