    package com.example.myapplicationsocketsam;

    import static android.content.ContentValues.TAG;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.app.Activity;
    import android.app.PendingIntent;
    import android.app.Service;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.IBinder;
    import android.telephony.SmsManager;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.TextView;

    import androidx.annotation.RequiresApi;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;


    public class MainActivity extends AppCompatActivity {
        private BroadcastReceiver messageReceiver;
        private static final int PERMISSION_REQUEST_CODE = 100;

        private TextView statusTextView;
        private Button button;
        private boolean isSending = false;

        @Override
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(messageReceiver);
        }


        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            statusTextView = findViewById(R.id.statusTextView);
            button=  findViewById(R.id.connectButton);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d("BUTTONS", "User tapped the Supabutton");
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("89650074309", null,"privet", null, null);

                }
            });
            Log.d("myTag","will send");

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] {Manifest.permission.SEND_SMS
                }, PERMISSION_REQUEST_CODE);
            }
            Log.d("myTag","connecting");

            Intent intent = new Intent(this, MyWebSocketService.class);
            intent.putExtra("URL", "ws://94.19.135.167/ws");
            Context context = getApplicationContext();
            startService(intent);

            registerReceiver(messageReceiver, new IntentFilter());

                registerReceiver(smsDeliveredReceiver, new IntentFilter("SMS_DELIVERED"),RECEIVER_NOT_EXPORTED);
                registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"),RECEIVER_NOT_EXPORTED);



                Log.d("myTag","connectiong");
                startForegroundService(intent);
            messageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String message = intent.getStringExtra("message");
                    button.setText(message);
                    Log.d("WebSocket", "Received message: " + message);
                }
            };

           // Log.d("MyTag", "Exception while doing ...");
           /* EdgeToEdge.enable(this);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });*/
        }



        private void startSendingSMS() {
            isSending = true;
            showLoader("Отправка SMS...");

            SmsManager smsManager = SmsManager.getDefault();
            String phoneNumber = "89650074309";
            String message = "privet";

            Intent sentIntent = new Intent("SMS_SENT");
            Intent deliveredIntent = new Intent("SMS_DELIVERED");

            PendingIntent sentPI = PendingIntent.getBroadcast(
                    this, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(
                    this, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE);

            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }
        private void showLoader(String message) {
          // gifLoader.setVisibility(View.VISIBLE);
            button.setBackgroundColor(Color.blue(3));
            statusTextView.setText(message);
        }

        private void hideLoader() {
            //gifLoader.setVisibility(View.GONE);
            button.setBackgroundColor(Color.green(3));
            statusTextView.setVisibility(View.GONE);
            isSending = false;
        }
        private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String phoneNumber = intent.getStringExtra("phoneNumber");
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        showLoader("SMS отправлен. Ожидание доставки...");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        showError("Ошибка отправки SMS");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        showError("Нет связи с сетью");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        showError("Ошибка данных SMS");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        showError("Радио выключено");
                        break;
                }
            }
        };

        private BroadcastReceiver smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String phoneNumber = intent.getStringExtra("phoneNumber");
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        showSuccess("SMS успешно доставлен!");
                        break;
                    case Activity.RESULT_CANCELED:
                        showError("SMS не доставлен");
                        break;
                }
                hideLoader();
            }
        };
        private void showError(String message) {
            showLoader(message);
            new Handler().postDelayed(this::hideLoader, 3000);
        }

        private void showSuccess(String message) {
            showLoader(message);
            new Handler().postDelayed(this::hideLoader, 2000);
        }
    }
