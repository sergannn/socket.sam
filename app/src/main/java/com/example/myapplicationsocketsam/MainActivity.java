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
        import android.telephony.SmsMessage;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

        import androidx.annotation.RequiresApi;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.content.ContextCompat;

        import com.koushikdutta.async.ArrayDeque;

        import java.util.Queue;


        public class MainActivity extends AppCompatActivity {
            private BroadcastReceiver messageReceiver;
            private static final int PERMISSION_REQUEST_CODE = 100;


            private Button button;

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

                Log.d("myTag", "Очередь сообщений очищена");
                button=  findViewById(R.id.connectButton);
                button.setText("Работаем");
                Log.d("myTag","will send");

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[] {Manifest.permission.SEND_SMS
                    }, PERMISSION_REQUEST_CODE);
                }
                Log.d("myTag","connecting");

                Intent intent = new Intent(this, MyWebSocketService.class);
                intent.putExtra("URL", "ws://94.19.135.167/ws");
                startService(intent);  // Вызываем только один раз



                // Создаем и регистрируем все receiver'ы
                messageReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        String message = intent.getStringExtra("message");
                        Log.d("broad",message);
                        button.setText(message);
                        Log.d("WebSocket", "Received message: " + message);
                    }
                };
                registerReceiver(messageReceiver, new IntentFilter("WEBSOCKET_MESSAGE"),RECEIVER_EXPORTED);

                BroadcastReceiver smsDeliveredReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                showSuccess("SMS успешно доставлен!");
                                Log.d("broad","SMS успешно доставлен!");
                                break;
                            case Activity.RESULT_CANCELED:
                                showError("SMS не доставлен");
                                break;
                        }
                    }
                };
                registerReceiver(smsDeliveredReceiver, new IntentFilter("SMS_DELIVERED"),RECEIVER_EXPORTED);

                BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
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
                            default:
                                Log.d("myTag", "default");
                        }
                    }
                };
                registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"),RECEIVER_EXPORTED);

                Log.d("myTag","connectiong");
                startForegroundService(intent);

            }




            private void showLoader(String message) {
                Log.d("myTag",message);
              // gifLoader.setVisibility(View.VISIBLE);
                button.setText(message);
             //   button.setBackgroundColor(Color.blue(3));
            }




            private void showError(String message) {
                showLoader(message);

            }

            private void showSuccess(String message) {
                showLoader(message);

            }
        }
