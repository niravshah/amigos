package com.amigos.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amigos.R;
import com.amigos.activity.NewJobRequestActivity;
import com.amigos.helpers.GDNConstants;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Nirav on 29/11/2015.
 */
public class GDNGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    int id = 946;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("gcm.notification.message");
        String details = data.getString("details");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message, details);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     * @param details
     */
    private void sendNotification(String message, String details) {
        Intent intent = new Intent(this, NewJobRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(GDNConstants.ACTION_DETAILS);
        intent.putExtra("details", details);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Intent rejectIntent = new Intent(this, NewJobRequestActivity.class);
        rejectIntent.setAction(GDNConstants.ACTION_REJECT);
        rejectIntent.putExtra("details", details);
        PendingIntent piDismiss = PendingIntent.getActivity(this, 0, rejectIntent, 0);

        Intent acceptIntent = new Intent(this, NewJobRequestActivity.class);
        acceptIntent.setAction(GDNConstants.ACTION_ACCEPT);
        acceptIntent.putExtra("details", details);
        PendingIntent piSnooze = PendingIntent.getActivity(this, 0, acceptIntent, 0);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("New Job Alert")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(defaultSoundUri)
                .setSmallIcon(R.drawable.ic_play_light)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("A new Job is available for you"))
                .addAction(R.drawable.ic_cancel_white_24dp,
                       "Reject", piDismiss)
                .addAction(R.drawable.ic_check_circle_white_24dp,
                        "Accept", piSnooze)
                .setAutoCancel(false);
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, notificationBuilder.build());
    }

}