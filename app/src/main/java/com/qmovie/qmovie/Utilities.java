package com.qmovie.qmovie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.text.DateFormat;
import java.util.Date;

public class Utilities
{

    public static final int IMAGE_QUALITY        = 10;

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image)
    {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static String getReadableLength(Context context, int minuets)
    {
        int hours = minuets / 60; //since both are ints, you get an int
        int minutes = minuets % 60;
        return context.getString(R.string.showLength, String.format("%d:%02d", hours, minutes));
    }

    public static String getHourFromDate(long dateInMilliseconds)
    {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
    }
}
