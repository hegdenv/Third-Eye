package com.example.visionpro;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Locale;

public class messeging extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_CODE_SPEECH_MSG = 101;
    private static final int REQUEST_CODE_SPEECH_PHONE = 102;
    private static final int PERMISSION_REQUEST_CODE = 111;
    private TextToSpeech tts;
    private EditText editTextMessage, editTextPhone;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messeging);

        tts = new TextToSpeech(this, this);
        editTextMessage = findViewById(R.id.editTextTextMultiLine);
        editTextPhone = findViewById(R.id.editTextPhone);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            receiveMsg();
        }

        editTextMessage.setOnClickListener(v -> {
            tts.speak("Please speak your message", TextToSpeech.QUEUE_FLUSH, null, null);
            editTextPhone.postDelayed(this::speakMsg, 3000);
        });

        editTextPhone.setOnClickListener(v -> {
            tts.speak("Please speak recipient's phone number", TextToSpeech.QUEUE_FLUSH, null, null);
            editTextPhone.postDelayed(this::speakPhone, 3000);
        });

        findViewById(R.id.sendMsg).setOnClickListener(v -> {
            String phoneNumber = editTextPhone.getText().toString();
            String message = editTextMessage.getText().toString();

            // Check if phone number or message is empty
            if (phoneNumber.isEmpty() || message.isEmpty()) {
                // Speak out if either field is empty
                tts.speak("Please enter phone number and message", TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                // Send message if both fields are filled
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, "ME", message, null, null);
                tts.speak("Message sent successfully", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

    }

    private void speakMsg() {
        Intent msgIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        msgIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        msgIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        msgIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message");
        startActivityForResult(msgIntent, REQUEST_CODE_SPEECH_MSG);
    }

    private void speakPhone() {
        Intent phoneIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        phoneIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        phoneIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        phoneIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak phone number");
        startActivityForResult(phoneIntent, REQUEST_CODE_SPEECH_PHONE);
    }

    private void receiveMsg() {
        BroadcastReceiver br = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for (SmsMessage sms : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        editTextPhone.setText(sms.getOriginatingAddress());
                        editTextMessage.setText(sms.getMessageBody());
                        Toast.makeText(getApplicationContext(), "Msg received", Toast.LENGTH_SHORT).show();
                        tts.speak("Message received from " + editTextPhone.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
                        tts.speak(editTextMessage.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }
        };
        registerReceiver(br, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (requestCode == REQUEST_CODE_SPEECH_MSG) {
                editTextMessage.setText(result.get(0));
            } else if (requestCode == REQUEST_CODE_SPEECH_PHONE) {
                editTextPhone.setText(result.get(0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
//            tts.setLanguage(Locale.US);
            int result = tts.setLanguage(new Locale("en", "IN"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(new Locale("kn", "IN")); // Kannada
            }
            tts.setSpeechRate(1.0f);
            tts.speak("You are in the messaging app. double Click on top to speak the mobile number, double Click on center to convey your message, And ,click on bottom to send message.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

}
