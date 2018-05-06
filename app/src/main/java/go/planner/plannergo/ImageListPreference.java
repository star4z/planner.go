package go.planner.plannergo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListAdapter;

/*
 * Created by SArnab©® on 18-01-2016.
 * Edited by SArnab©® on 28-01-2016.
 */
public class ImageListPreference extends ListPreference {

    private Drawable[] mEntryImages;

    /**
     * Constructor of the ImageListPreference. Initializes the preference using Java.
     * @param context application context.
     */
    public ImageListPreference(Context context) {
        super(context);
    }

    /**
     * Constructor of the ImageListPreference. Initializes the custom images.
     * @param context application context.
     * @param attrs custom xml attributes.
     */
    public ImageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference);
            String[] imageNames = context.getResources().getStringArray(typedArray.getResourceId(typedArray.getIndexCount() - 1, -1));
            int[] _resourceIds = new int[imageNames.length];
            Bitmap[] bitmaps = new Bitmap[_resourceIds.length];
            for (int i = 0; i < imageNames.length; i++) {
                String imageName = imageNames[i].substring(
                        imageNames[i].lastIndexOf('/') + 1,
                        imageNames[i].lastIndexOf('.'));
                _resourceIds[i] = context.getResources().getIdentifier(imageName,
                        "drawable", context.getPackageName());
                bitmaps[i] = BitmapFactory.decodeResource(getContext().getResources(), _resourceIds[i]);
            }
            boolean setRoundedImage = typedArray.getBoolean(R.styleable.ImageListPreference_roundedImage, true);
            setEntryImages(bitmaps, setRoundedImage);
            typedArray.recycle();
        } catch (Exception e) {
            Log.println(Log.ERROR, "ImageListPref", e.getMessage());
        }
    }

    public void setEntryImages(Bitmap[] entryImages, boolean setRoundedImage) {
        if (setRoundedImage) {
            RoundedBitmapDrawable[] roundedEntryImages = new RoundedBitmapDrawable[entryImages.length];
            for (int i = 0; i < entryImages.length; i++) {
                roundedEntryImages[i] = RoundedBitmapDrawableFactory.create(getContext().getResources(), entryImages[i]);
                roundedEntryImages[i].setAntiAlias(true);
                roundedEntryImages[i].setCircular(true);
            }
            setEntryImages(roundedEntryImages);
        } else
            setEntryImages(entryImages);
    }

    public void setEntryImages(Drawable[] entryImages, boolean setRoundedImage) {
        if (setRoundedImage) {
            RoundedBitmapDrawable[] roundedEntryImages = new RoundedBitmapDrawable[entryImages.length];
            for (int i = 0; i < entryImages.length; i++) {
                Bitmap bitmap = ((BitmapDrawable) entryImages[i]).getBitmap();
                roundedEntryImages[i] = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
                roundedEntryImages[i].setAntiAlias(true);
                roundedEntryImages[i].setCircular(true);
            }
            mEntryImages = roundedEntryImages;
        } else
            mEntryImages = entryImages;
    }

    private void setEntryImages(Bitmap[] entryImages) {
        BitmapDrawable[] drawables = new BitmapDrawable[entryImages.length];
        for (int i = 0; i < entryImages.length; i++) {
            drawables[i] = new BitmapDrawable(getContext().getResources(), entryImages[i]);
        }
        setEntryImages(drawables);
    }

    private void setEntryImages(Drawable[] entryImages) {
        mEntryImages = entryImages;
    }

    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "0"));
        ListAdapter listAdapter;
        listAdapter = new ImageArrayAdapter(getContext(), getEntries(), mEntryImages, index);
        builder.setAdapter(listAdapter, this);
        builder.setTitle(getDialogTitle());
        builder.setNegativeButton(android.R.string.cancel, null);
        super.onPrepareDialogBuilder(builder);
    }
}