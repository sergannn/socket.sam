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
    private static final int SMS_TIMEOUT = 30000; // 30 секунд
    public WebSocketClient(Context context) {
        this.context = context.getApplicationContext();

    }

    public synchronized boolean connect(String url) {


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
                    //isConnected = true;
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    Log.d(TAG, "Received message: " + text);
                    Intent intent = new Intent("WEBSOCKET_MESSAGE");
                    intent.putExtra("message", text);
                    context.sendBroadcast(intent);
                    String[] b = text.split("/");
                    sendSMS(b[1],b[0]);
                }
                private boolean sendSMS(String message, String phoneNumber) {

                    Intent sentIntent = new Intent("SMS_SENT");
                    Intent deliveredIntent = new Intent("SMS_DELIVERED");

                    PendingIntent sentPI = PendingIntent.getBroadcast(
                            context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);
                    PendingIntent deliveredPI = PendingIntent.getBroadcast(
                            context, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE);
                    try {

                        Log.d("myTag","will send");
                        Log.d("myTag",phoneNumber);
                  //      addToSmsQueue(new SmsMessage(message, phoneNumber));
                        SmsManager smsManager = SmsManager.getDefault();
                        Log.d("myTag","i will send to " + phoneNumber + ':'+message);

                        smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

                        // Добавляем broadcast для уведомления об отправке
                        Intent statusIntent = new Intent("SMS_STATUS");
                        statusIntent.putExtra("status", "sent");

                        context.sendBroadcast(statusIntent);
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending SMS to " + phoneNumber, e);
                        return false;
                    }
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                    Log.e(TAG, "WebSocket connection failed", t);

                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    Log.d(TAG, "WebSocket closing");
                }

                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    Log.d(TAG, "WebSocket closed");

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

        }
    }

    }
