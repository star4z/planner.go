package go.planner.plannergo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Context c;

    /**
     * Constructor
     *
     * @param activity      used for context and resource calls
     * @param drawerOptions stores the text for all items
     * @param drawerIcons   stores the icons for all items
     * @param selectedPos   stores the currently active view
     */
    DrawerAdapter(Activity activity, String[] drawerOptions, int[] drawerIcons, int selectedPos) {
        this.drawerOptions = drawerOptions;
        this.drawerIcons = drawerIcons;
        this.selectedPos = selectedPos;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        res = activity.getResources();
        c = activity;
    }

    /**
     * Sets selectedPos to the input value
     *
     * @param selectedPos indicates which item in the drawer is "selected"
     */
    void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
    }

    /**
     * Retrieves the number of items in the adapter
     * @return length of drawerOptions array
     */
    @Override
    public int getCount() {
        return drawerOptions.length;
    }

    /**
     * Returns the appropriate text corresponding to the input position
     * @param position position of item where
     * @return the item from drawerOptions at the designated position
     */
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
                inflater.inflate(R.layout.view_drawer_list_item_selected, parent, false) :
                inflater.inflate(R.layout.view_drawer_list_item, parent, false);

        TextView text = view.findViewById(R.id.nBar_item_text);
        ImageView icon = view.findViewById(R.id.nBar_item_icon);


        if (c instanceof ColorSchemeActivity) {
            ColorScheme scheme = ((ColorSchemeActivity) c).getColorScheme();
            if (position == 0) {
                if (scheme == ColorScheme.SCHEME_DARK)
                    view.setBackgroundColor(scheme.getColor(ColorScheme.PRIMARY_DARK));
                else
                    view.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));
            } else {
                view.setBackgroundColor(scheme.getColor(ColorScheme.PRIMARY));
                text.setTextColor(scheme.getColor(ColorScheme.TEXT_COLOR));
            }
            if (position == selectedPos) {
                LinearLayout l = view.findViewById(R.id.nBar_item);
                Drawable d = l.getBackground();
                d.setTint(scheme.getColor(ColorScheme.ASSIGNMENT_VIEW_BG));
            }
        }


        switch (position) {
            case 0:
                text.setTextColor(ContextCompat.getColor(c, R.color.textWhite));
                int statusBarHeight = (int) Math.floor(25 * res.getDisplayMetrics().density);
                Log.d(TAG, "StatusBarHeight=" + statusBarHeight);

                final float scale = c.getResources().getDisplayMetrics().density;
                int pixels = (int) (16 * scale + 0.5f);

                view.setPadding(view.getPaddingLeft(), statusBarHeight + pixels - 3, view.getPaddingRight(),
                        view.getPaddingBottom());
                break;
            case 1:
                text.setTextColor(res.getColor(R.color.nav_color_1));
                break;
            case 2:
                text.setTextColor(res.getColor(R.color.nav_color_2));
                break;
            case 3:
                text.setTextColor(res.getColor(R.color.nav_color_3));
                break;
        }

        text.setText(drawerOptions[position]);
        icon.setImageResource(drawerIcons[position]);

        return view;
    }
}
