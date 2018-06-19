package go.planner.plannergo;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

public class DropDownEditText extends android.support.v7.widget.AppCompatEditText {
    Bag<String> list;
    ListView listView;

    public DropDownEditText(Context c, AttributeSet attrs) {
        super(c, attrs);
        listView = new ListView(c);
    }


    public DropDownEditText(Context c, Bag<String> list) {
        super(c);
        this.list = list;
        listView = new ListView(c);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            displayList();
        } else {
            hideList();
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    void displayList(){
        int[] coors = new int[2];
        getLocationOnScreen(coors);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        Log.v("DDEditText", "x:" + coors[0] + "y:" + coors[1] + "screenY:" + screenHeight);
        if (screenHeight - coors[1] > coors[1]) {
            Log.v("DDEditText", "Display below");
            if (getParent() != null){

            }

        } else {
            Log.v("DDEditText", "Display above");

        }
    }

    void hideList(){

    }

    public void setList(Bag<String> list) {
        this.list = list;
    }
}
