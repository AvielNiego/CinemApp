package com.qmovie.qmovie.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.qmovie.qmovie.R;

public class RemainderReceiver extends BroadcastReceiver
{
    public static final  String MOVIE_NAME_EXTRA_KEY = "movie_name";
    private static final int    NOTIFICATION_ID      = 1;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        String movieName = intent.getExtras().getString(MOVIE_NAME_EXTRA_KEY);
        String contentText = context.getString(R.string.movie_remainder_text, movieName);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.movie_remainder_title))
                .setContentText(contentText)
                .setGroup(MOVIE_NAME_EXTRA_KEY)
                .setGroupSummary(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setAutoCancel(true)
        .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(contentText));

        Intent resultIntent = new Intent(context, MovieDetailActivity.class);
        resultIntent.setData(intent.getData());

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // the application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MovieDetailActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(movieName, NOTIFICATION_ID, notificationBuilder.build());
    }
}
