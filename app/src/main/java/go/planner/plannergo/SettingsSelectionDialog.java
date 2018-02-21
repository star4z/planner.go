package go.planner.plannergo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Produces a pop-up which lets the user manipulate a setting.
 * Created by bdphi on 1/30/2018.
 */

public class SettingsSelectionDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

//        View view = initializeViews(getArguments());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings_body,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);
        LinearLayout parent = view.findViewById(R.id.body);

        String[] titles = getArguments().getStringArray("options");
        String[] subtitles = getArguments().getStringArray("descriptions");
        int selectedIndex = getArguments().getInt("selectedIndex");

        assert titles != null && subtitles != null;

        /*
        xTODO: Add functionality:
        when an item is selected, dialog closes and the activity receives and int position of the selected item.
        When the dialog opens, the already chosen item is selected.
         */

        for (int i = 0; i < titles.length; i++){
            final int index = i;
            View dialogOption = inflater.inflate(R.layout.view_settings_dialog_option, null);
            RelativeLayout nextLayout = dialogOption.findViewById(R.id.parent_item);

            RadioButton radio = nextLayout.findViewById(R.id.radio);
            TextView title = nextLayout.findViewById(R.id.title);
            TextView subtitle = nextLayout.findViewById(R.id.subtitle);

            if (index == selectedIndex){
                radio.toggle();
            }

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SettingsActivity activity = (SettingsActivity) getActivity();
                    activity.defaultSortIndex = index;
                    activity.updateViews();
                    SettingsSelectionDialog.this.dismiss();
                }
            };

            nextLayout.setOnClickListener(listener);
            radio.setOnClickListener(listener);


            title.setText(titles[i]);
            subtitle.setText(subtitles[i]);
            parent.addView(dialogOption);

        }

        builder.setView(view)
                .setTitle(getArguments().getString("title"));

        return builder.create();
    }

}