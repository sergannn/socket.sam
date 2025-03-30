package com.example.myapplicationsocketsam;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
public class WebSocketClient {
    private static final String TAG = "WebSocketClient";
    private WebSocket ws;
    private Context context;
    private boolean isConnected = false;
    private Queue<SmsMessage> smsQueue;
    private Map<String, Boolean> deliveryStatuses;
    public WebSocketClient(Context context) {
        this.context = context.getApplicationContext();
        this.smsQueue = new LinkedList<>();
        this.deliveryStatuses = new HashMap<>();
        registerReceivers();
    }

    public synchronized boolean connect(String url) {
        if (isConnected) {
            Log.w(TAG, "Already connected, disconnecting first");
            disconnect();
        }

        Log.d(TAG, "Connecting to WebSocket...");
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(15, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            ws = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    Log.d(TAG, "WebSocket connection opened");
                    isConnected = true;
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    Log.d(TAG, "Received message: " + text);
                    sendSMS("hello","89219404309");
                }
                private boolean sendSMS(String message, String phoneNumber) {

                    Intent sentIntent = new Intent("SMS_SENT");
                    Intent deliveredIntent = new Intent("SMS_DELIVERED");

                    PendingIntent sentPI = PendingIntent.getBroadcast(
                            context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);
                    PendingIntent deliveredPI = PendingIntent.getBroadcast(
                            context, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE);
                    try {
                        Log.d("myTag","there are "+smsQueue.size());
                        Log.d("myTag","will send");
                        Log.d("myTag",phoneNumber);
                  //      addToSmsQueue(new SmsMessage(message, phoneNumber));
                        SmsManager smsManager = SmsManager.getDefault();

                        smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending SMS to " + phoneNumber, e);
                        return false;
                    }
                }
                private void addToSmsQueue(String message, String phoneNumber) {
                   // SmsMessage sms = new SmsMessage(message, phoneNumber);
                   // smsQueue.add(sms);
                    Log.d("myTag", "Added to queue: " + smsQueue.size() + " messages");
                }
                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                    Log.e(TAG, "WebSocket connection failed", t);
                    isConnected = false;
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    Log.d(TAG, "WebSocket closing");
                }

                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    Log.d(TAG, "WebSocket closed");
                    isConnected = false;
                }
            });
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to WebSocket", e);
            return false;
        }
    }

    public void disconnect() {
        if (ws != null) {
            ws.close(1000, "Normal closure");
            ws = null;
            isConnected = false;
        }
    }
    private void registerReceivers() {
        // Register receivers for sent and delivered status
     /*   context.registerReceiver(sentReceiver,
                new IntentFilter(SMS_SENT_ACTION));
        context.registerReceiver(deliveredReceiver,
                new IntentFilter(SMS_DELIVERED_ACTION));*/
    }
    private void unregisterReceivers() {
        /*try {
            context.unregisterReceiver(sentReceiver);
            context.unregisterReceiver(deliveredReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receivers already unregistered");
        }*/
    }
}