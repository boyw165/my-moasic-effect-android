package boyw165.com.my_mosaic_stickers.tool;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;

public class ImageUtil {
    private final static String TAG = "ImageUtil";

    private final static double MAX_SAMPLED_WIDTH = 180;
    private final static double MAX_SAMPLED_HEIGHT = 180;

    /**
     * Get original size of a given image coming with EXIF orientation resolved.
     *
     * @param context To get content resolver in case of that we have no
     *                permission to access the given URI.
     */
    public static Rect getOriginalSize(Context context, String url) {
        return getOriginalSize(context, Uri.parse(url));
    }

    /**
     * Get original size of a given image coming with EXIF orientation resolved.
     *
     * @param context To get content resolver in case of that it has no permission
     *                to access the given URI produced by other Apps.
     */
    public static Rect getOriginalSize(Context context, Uri uri) {
        try {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),
                                       null, option);

            int width;
            int height;
            int exifOrientation = getExifOrientation(uri.getPath());
            if (exifOrientation == 90 || exifOrientation == 270) {
                width = option.outHeight;
                height = option.outWidth;
            } else {
                width = option.outWidth;
                height = option.outHeight;
            }

            return new Rect(0, 0, width, height);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    /**
     * @param context To get content resolver in case of that it has no permission
     *                to access the given URI produced by other Apps.
     */
    public static int getSamplingSize(Context context, Uri uri) {
        Rect size = getOriginalSize(context, uri);
        int sampleSize = 1;

        if (size.width() > MAX_SAMPLED_WIDTH || size.height() > MAX_SAMPLED_HEIGHT) {
            double boundAspect = MAX_SAMPLED_WIDTH / MAX_SAMPLED_HEIGHT;
            double bmpAspect = size.width() / size.height();
            double scale = boundAspect > bmpAspect ?
                MAX_SAMPLED_HEIGHT / size.height() :
                MAX_SAMPLED_WIDTH / size.width();
            double roughSampleSize = Math.round(1d / scale);

            sampleSize = (int) Math.pow(2, Math.ceil(Math.sqrt(roughSampleSize)));
        }

        return sampleSize;
    }

    /**
     * Return a sampled bitmap according to sampling size, getSamplingSize().
     * @param context To get content resolver in case of that it has no permission
     *                to access the given URI produced by other Apps.
     */
    public static Bitmap getSampledBitmap(Context context, Uri uri) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = getSamplingSize(context, uri);

        try {
            return BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(uri),
                null, option);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    /**
     * Query database by given URI to get orientation of the image in MediaStore.
     * If the information is not available in MediaStore, gets from Exif information.
     * Possible values: 0, 90, 180, 270.
     * Returns 0 if can't get the information.
     */
    public static int getOrientation(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver()
                               .query(uri,
                                      new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
                                      null, null, null);

        try {
            if (cursor == null || cursor.getCount() != 1 || !cursor.moveToFirst()) {
                // Can't get info from this cursor, get from exif information
                return getExifOrientation(uri.getPath());
            }
            return cursor.getInt(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get orientation of the image by given cursor pointing to the image.
     */
    public static int getOrientation(Cursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor is null.");
        }

        int orIndex = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
        if (orIndex >= 0) {
            return cursor.getInt(orIndex);
        } else {
            int pathIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            if (pathIndex >= 0) {
                return getExifOrientation(cursor.getString(pathIndex));
            } else {
                return 0;
            }
        }
    }

    /**
     * Get orientation of the image from Exif information.
     * Possible values: 0, 90, 180, 270.
     * Returns 0 if can't get the information.
     */
    public static int getExifOrientation(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }

        try {
            ExifInterface exif = new ExifInterface(filePath);
            int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return 0;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            return 0;
        }
    }
}
