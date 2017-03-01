package th.co.gosoft.go10.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by manitkannika on 2/20/2017 AD.
 */

public class DownloadImageUtils {

    private static final String LOG_TAG = "DownloadImageUtils";
    private static int resourceId ;
    private Context context;

    public static void setImageAvatar(Context context, ImageView imageView, String imageName) {
        Log.i(LOG_TAG, "setImageAvatar");
        String URL = PropertyUtility.getProperty("httpUrlSite", context )+"GO10WebService/DownloadServlet";
        getResourceFromURL(context, imageView, imageName, URL, false);
    }

    public static void setImageRoom(Context context, ImageView imageView, String imageName) {
        Log.i(LOG_TAG, "setImageRoom");
        String URL = PropertyUtility.getProperty("httpUrlSite", context )+"GO10WebService/DownloadServlet";
        getResourceFromURL(context, imageView, imageName, URL, true);
    }

    private static void getResourceFromURL(final Context context, final ImageView imageView, String imageName, String URL, boolean flag) {
        final Resources resources = context.getResources();
        if(isExitInDrawable(context, imageName)) {
            resourceId = resources.getIdentifier(imageName, "drawable", context.getPackageName());
            imageView.setImageResource(resourceId);
        } else {
            if(flag) {
                imageName = concatFileType(imageName);
            }
            String imageURL = URL + "?imageName="+imageName;
            Log.i(LOG_TAG,"Loading Image : "+imageURL);

            if(flag) {
                Glide.with(context)
                        .load(imageURL)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
            } else {
                Glide.with(context)
                        .load(imageURL)
                        .fitCenter()
                        .bitmapTransform(new CropCircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
            }

        }
    }

    private static String concatFileType(String imageName) {
        return imageName+".png";
    }

    private static boolean isExitInDrawable(Context context, String fileName) {
        Resources resources = context.getResources();
        resourceId = resources.getIdentifier(fileName, "drawable",
                context.getPackageName());
        return resourceId != 0;
    }

}
