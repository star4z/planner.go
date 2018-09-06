package go.planner.plannergo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Formats list to be displayed in navigation drawer.
 * Created by bdphi on 1/7/2018.
 */

public class DrawerAdapter extends BaseAdapter {

    private static final String TAG = "DrawerAdapter";

    private Resources res;
    private String[] drawerOptions;
    private int[] drawerIcons;
    private static LayoutInflater inflater = null;
    private int selectedPos;

    DrawerAdapter(Activity activity, String[] drawerOptions, int[] drawerIcons, int selectedPos) {
        this.drawerOptions = drawerOptions;
        this.drawerIcons = drawerIcons;
        this.selectedPos = selectedPos;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        res = activity.getResources();
    }

    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
    }

    @Override
    public int getCount() {
        return drawerOptions.length;
    }

    @Override
    public Object getItem(int position) {
        if (position < drawerOptions.length)
            return drawerOptions[position];
        else
            return -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = (position == selectedPos) ?
                inflater.inflate(R.layout.view_drawer_list_item_selected, null) :
                inflater.inflate(R.layout.view_drawer_list_item, null); //I'm not sure why but this only works when the root is null else the app crashes

//            View view = convertView;
//            if (convertView == null) {
//                Log.v(TAG, "convertView was null");
//                view = inflater.inflate(R.layout.view_drawer_list_item, parent); //I'm not sure why but this only works when the root is null else the app crashes
//            } else {
//                Log.v(TAG, "convertView was !null");
//            }



        TextView text = view.findViewById(R.id.nBar_item_text);
        ImageView icon = view.findViewById(R.id.nBar_item_icon);

        switch (position) {
            case 0:
                view.setBackgroundColor(ColorPicker.getColorPrimary());
                text.setTextColor(ColorPicker.getColorPrimaryText());
                break;
            case 1:
                text.setTextColor(res.getColor(R.color.golden));
                break;
            case 2:
                text.setTextColor(res.getColor(R.color.green));
                break;
            case 3:
                text.setTextColor(res.getColor(R.color.maroon));
                break;
        }

        text.setText(drawerOptions[position]);
        icon.setImageResource(drawerIcons[position]);

        return view;
    }
}
