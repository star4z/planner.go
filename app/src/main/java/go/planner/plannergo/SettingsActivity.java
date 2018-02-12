package go.planner.plannergo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Enables user to modify settings for app
 * Created by bdphi on 1/30/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    final String FILE_NAME = "planner.settings";

    int defaultSortIndex = 0;
    boolean overdueFirst = true;
    String sortOptions[] =  new String[]{"Sort by date", "Sort by class", "Sort by title", "Sort by type"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        readSettings();
        updateViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark));
        setSupportActionBar(toolbar);

    }

    /**
     * Handles any option menu items; probably will only handle up functionality
     * Handles storing data in file that was manipulated.
     * @param item selected item; will always be home unless menu is defined
     * @return true if action performed; else calls super
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Returns to the Main activity
            case android.R.id.home:
                overdueFirst = !((Switch) findViewById(R.id.overdue_switch)).isChecked();
                writeSettings();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void writeSettings() {
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            boolean fileCreated = file.createNewFile();
            if (fileCreated)
                Log.v("MA", "File did not exist and was created.");

            FileOutputStream fos = new FileOutputStream(file, false);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeInt(defaultSortIndex);
                oos.writeBoolean(overdueFirst);

                oos.close();

            } catch (IOException e) {
                Log.v("MA", "File did not process");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.v("MA", "File not found to write");
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("MA", "Could not create file for some reason");
            e.printStackTrace();
        }
    }

    public void readSettings() {
        ObjectInputStream inputStream;

        try {
            File file = new File(getFilesDir(), FILE_NAME);
            inputStream = new ObjectInputStream(new FileInputStream(file));

            defaultSortIndex = inputStream.readInt();
            overdueFirst = inputStream.readBoolean();

            inputStream.close();
        } catch (EOFException e) {
            Log.v("MainActivity.read", "End of stream reached.");
        } catch (IOException e) {
            Log.v("MainActivity.read", "The file was not to be found.");
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog with a series of titles, descriptions, with radio button to indicate last
     * checked item.
     * For use with items that have more than two possible states.
     * Method should be called from xml
     * @param view View that received click
     */
    public void openMenuDialog(View view){
        String[] options;
        String[] descriptions;
        String tag = "";

        SettingsSelectionDialog dialog = new SettingsSelectionDialog();
        Bundle bundle = new Bundle();

        switch (view.getId()){
            case (R.id.setting_sort_type):
                options = sortOptions;
                descriptions = new String[]{
                "Assignments are grouped by date; \nEarliest assignment is listed first.",
                "Assignments are grouped by class. \nClasses are listed alphabetically.",
                "Assignments are listed alphabetically by title.",
                "Assignments are grouped by type."};

                bundle.putStringArray("options",options);
                bundle.putStringArray("descriptions",descriptions);
                bundle.putString("title","Default Sort");
                bundle.putInt("selectedIndex",defaultSortIndex);

                tag = "Settings: Sort Type";
                break;
        }

        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(),tag);
    }

    public void overdueSwitchToggle(View view){
        Switch aSwitch = findViewById(R.id.overdue_switch);
        aSwitch.toggle();
    }

    /**
     * For use in initializing the Activity and returning the Activity to focus after a dialog
     * has been opened.
     */
    public void updateViews(){
//        overdueFirst = ((Switch)findViewById(R.id.overdue_switch)).isChecked();
        writeSettings();
        TextView defaultSortSelection = findViewById(R.id.selected_sort_type);
        defaultSortSelection.setText(sortOptions[defaultSortIndex]);
        Switch overdueSwitch = findViewById(R.id.overdue_switch);
        overdueSwitch.setChecked(!overdueFirst);
    }

    public void about(View view){
        startActivity(new Intent( this, AboutActivity.class));
    }

}
