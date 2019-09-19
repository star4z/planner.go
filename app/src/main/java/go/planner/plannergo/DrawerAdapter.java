package go.planner.plannergo;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;

/**
 * Formats list to be displayed in navigation drawer.
 * Created by bdphi on 1/7/2018.
 */

public class DrawerAdapter extends BaseAdapter {
    private static final String TAG = "DrawerAdapter";

    private String[] drawerOptions;
    private int[] drawerIcons;
    private static LayoutInflater inflater = null;
    private int selectedPos;
    private Context c;
    GoogleSignInAccount account = null;

    /**
     * Constructor
     *
     * @param activity      used for context and resource calls
     * @param drawerOptions stores the text for all items
     * @param selectedPos   stores the currently active view
     */
    DrawerAdapter(Activity activity, String[] drawerOptions, int[] drawerIcons, int selectedPos) {
        this.drawerOptions = drawerOptions;
        this.drawerIcons = drawerIcons;
        this.selectedPos = selectedPos;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
     *
     * @return length of drawerOptions array
     */
    @Override
    public int getCount() {
        return drawerOptions.length;
    }

    /**
     * Returns the appropriate text corresponding to the input position
     *
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
        final View view;
        ColorScheme scheme = ((ColorSchemeActivity) c).getColorScheme();


        if (position == MainActivity.iHeader) {
            view = inflater.inflate(R.layout.view_drawer_header, parent, false);
        } else if (position == MainActivity.iSignIn) {
            view = inflater.inflate(R.layout.view_drawer_sign_in_button, parent, false);
        } else if (position == selectedPos) {
            view = inflater.inflate(R.layout.view_drawer_list_item_selected, parent, false);
        } else {
            view = inflater.inflate(R.layout.view_drawer_list_item, parent, false);
        }

        view.setBackgroundColor(scheme.getColor(c, Field.DW_BG));

        if (position != MainActivity.iSignIn) {

            TextView text = view.findViewById(R.id.nBar_item_text);
            ImageView icon = view.findViewById(R.id.nBar_item_icon);

            if (position == selectedPos) {
                LinearLayout l = view.findViewById(R.id.nBar_item);
                Drawable d = l.getBackground();
                d.setTint(scheme.getColor(c, Field.DW_SELECT_BG));
            }


            switch (position) {
                case MainActivity.iHeader:
                    view.setBackgroundColor(scheme.getColor(c, Field.DW_HEAD_BG));
                    text.setTextColor(scheme.getColor(c,Field.DW_HEAD_TEXT));
                    break;
                case MainActivity.iInProgress:
                    text.setTextColor(scheme.getColor(c, Field.DW_IP_TEXT));
                    break;
                case MainActivity.iCompleted:
                    text.setTextColor(scheme.getColor(c, Field.DW_CP_TEXT));
                    break;
                case MainActivity.iTrash:
                    text.setTextColor(scheme.getColor(c, Field.DW_TR_TEXT));
                    break;
                default:
                    text.setTextColor(scheme.getColor(c, Field.DW_OT_TEXT));
                    break;
            }

            text.setText(drawerOptions[position]);
            icon.setImageResource(drawerIcons[position]);
        } else {
            SignInButton signInButton = view.findViewById(R.id.sign_in_button);
            TextView userName = view.findViewById(R.id.user_name);
            TextView signOutButton = view.findViewById(R.id.sign_out_button);

            userName.setTextColor(scheme.getColor(c, Field.DW_OT_TEXT));
            signOutButton.setTextColor(scheme.getColor(c, Field.DW_OT_TEXT));

            if (account != null) {
                signInButton.setVisibility(View.GONE);
                userName.setText(account.getEmail());
                userName.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.VISIBLE);
                signOutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "Clicked sign out button.");
                        if (c instanceof SignInActivity) {
                            ((SignInActivity) c).signOut(v);
                        }
                    }
                });
            } else {
                userName.setVisibility(View.GONE);
                signOutButton.setVisibility(View.GONE);
                signInButton.setVisibility(View.VISIBLE);
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "Clicked sign in button.");
                        if (c instanceof SignInActivity) {
                            ((SignInActivity) c).signIn(v);
                        }
                    }
                });
            }
        }

        return view;
    }
}
