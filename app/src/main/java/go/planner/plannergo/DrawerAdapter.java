package go.planner.plannergo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

    private String[] drawerOptions;
    private int[] drawerIcons;
    private static LayoutInflater inflater = null;

    DrawerAdapter(Activity activity, String[] drawerOptions, int[] drawerIcons) {
        this.drawerOptions = drawerOptions;
        this.drawerIcons = drawerIcons;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null)
            view = inflater.inflate(R.layout.view_drawer_list_item, null); //I'm not sure why but this only works when the root is null else the app crashes

        TextView text = view.findViewById(R.id.nBar_item_text);
        ImageView icon = view.findViewById(R.id.nBar_item_icon);

        text.setText(drawerOptions[position]);
        icon.setImageResource(drawerIcons[position]);

        return view;
    }
}
