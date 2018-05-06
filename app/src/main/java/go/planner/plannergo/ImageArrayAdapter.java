package go.planner.plannergo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

/*
 * Created by SArnab©® on 18-01-2016.
 * Edited by SArnab©® on 28-01-2016.
 */
public class ImageArrayAdapter extends ArrayAdapter<CharSequence> {

    private int _index = 0;
    private Drawable[] entryImages;
    Context mContext;

    public ImageArrayAdapter(Context context, CharSequence[] objects, Drawable[] mEntryImages, int i) {
        super(context, R.layout.image_list_item, objects);
        _index = i;
        entryImages = mEntryImages;
        mContext = context;
    }

    static class ViewHolder {
        ImageView image;
        CheckedTextView check;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.image_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.check = (CheckedTextView) convertView.findViewById(R.id.check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (entryImages != null) {
            try {
                viewHolder.image.setImageDrawable(entryImages[position]);
            } catch (Exception e) {
                Log.println(Log.ERROR, "ImageListPreference", e.getMessage());
            }
        }

        viewHolder.check.setText(getItem(position));

        if (position == _index) {
            viewHolder.check.setChecked(true);
        }

        return convertView;
    }
}
