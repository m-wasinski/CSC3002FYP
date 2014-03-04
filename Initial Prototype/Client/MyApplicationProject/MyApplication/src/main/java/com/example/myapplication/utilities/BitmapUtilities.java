package com.example.myapplication.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by Michal on 02/03/14.
 */
public class BitmapUtilities
{
    /**
     * Decodes the image from specified path.
     * Credit goes to the original author Andreas Agvard.
     * Referenced from the following article: http://developer.sonymobile.com/2011/06/27/how-to-scale-images-for-your-android-application/
     **/
    public static Bitmap decodeFile(String pathName, int dstWidth, int dstHeight, ScalingLogic scalingLogic)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight, scalingLogic);

        return BitmapFactory.decodeFile(pathName, options);
    }

    public static Bitmap rescaleBitmap(Bitmap bm, int dstWidth, int dstHeight)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) dstWidth) / width;
        float scaleHeight = ((float) dstHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;
    }

    /**
     * Used for calculating sample size for images when resizing in order to preserve their aspect radio.
     * Credit goes to the original author: Andreas Agvard.
     * Referenced from the following article: http://developer.sonymobile.com/2011/06/27/how-to-scale-images-for-your-android-application/
     **/
    private static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic)
    {
        if (scalingLogic == ScalingLogic.FIT)
        {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;

            if (srcAspect > dstAspect)
            {
                return srcWidth / dstWidth;
            }
            else
            {
                return srcHeight / dstHeight;
            }
        }
        else
        {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;

            if (srcAspect > dstAspect)
            {
                return srcHeight / dstHeight;
            }
            else
            {
                return srcWidth / dstWidth;
            }
        }
    }

    public enum ScalingLogic
    {
        FIT,
        CROP
    }
}
