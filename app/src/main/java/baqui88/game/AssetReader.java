package baqui88.game;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

public class AssetReader {
    public static String loadJsonFromAsset(String filename, Context context) {
        String json = null;

        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static Bitmap getBitmapFromAssets(Context context, String filePath, boolean isMutable) {
        Bitmap bitmap = null;
        InputStream istr = null;
        try {
            istr = context.getAssets().open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
            if (isMutable)
                // Warnings : This way too much memory consumption on large images
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(istr != null) {
                try {
                    istr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public static Drawable getDrawableFromAssets(Context context, String filePath){
        Drawable drawable = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(filePath);
            drawable = Drawable.createFromStream(inputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return drawable;
    }

}
