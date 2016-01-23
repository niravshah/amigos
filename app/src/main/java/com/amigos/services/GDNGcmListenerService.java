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
import com.amigos.activity.MainActivity;
import com.amigos.activity.NewJobRequestActivity;
import com.amigos.helpers.GDNConstants;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;

/**
 * Created by Nirav on 29/11/2015.
 */
public class GDNGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    private String jobId;
    private String address;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String type = data.getString("type");
        String message = data.getString("gcm.notification.message");
        String details = data.getString("details");

        switch (type){
            case "PAYMENT":
                processPaymentNotification(details, message);
                break;
            case "JOB":
                processJobNotification(details,message);
                break;
            default:
                break;

        }

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

    }

    private void processJobNotification(String details, String message) {
        String[] parts = details.split(":");
        jobId = parts[1];
        address = parts[6];

        sendJobNotification(message, details);

    }

    private void processPaymentNotification(String details, String message) {
        sendNotification(message,details);
    }

    private void sendNotification(String message, String details) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_dn_notification)
                .setContentTitle(message)
                .setContentText(details)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     * @param details
     */
    private void sendJobNotification(String message, String details) {
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
                .setContentText(address)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(defaultSoundUri)
                .setSmallIcon(R.drawable.ic_stat_dn_notification)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(address))
                .addAction(R.drawable.ic_cancel_white_24dp,
                       "Reject", piDismiss)
                .addAction(R.drawable.ic_check_circle_white_24dp,
                        "Accept", piSnooze);


        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }

}