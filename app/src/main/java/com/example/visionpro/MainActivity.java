package com.example.visionpro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private LinearLayout msgBox, phoneMngr, timeDate, cameraCard;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, this);

        // Attach click & long-click listeners to each card
        msgBox = findViewById(R.id.msgBox);
        phoneMngr = findViewById(R.id.phoneMngr);
        timeDate = findViewById(R.id.timeDate);
//        cameraCard = findViewById(R.id.cameraCard);

        if (msgBox != null) {
            msgBox.setOnClickListener(this);
            msgBox.setOnLongClickListener(this);
        }
        if (phoneMngr != null) {
            phoneMngr.setOnClickListener(this);
            phoneMngr.setOnLongClickListener(this);
        }
        if (timeDate != null) {
            timeDate.setOnClickListener(this);
            timeDate.setOnLongClickListener(this);
        }
//        if (cameraCard != null) {
//            cameraCard.setOnClickListener(this);
//            cameraCard.setOnLongClickListener(this);
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        String text = "";

        if (view == msgBox) {
            text = "Message Inbox";
        } else if (view == phoneMngr) {
            text = "Phone Manager";
        } else if (view == timeDate) {
            text = "Battery Status";
        }
//        else if (view == cameraCard) {
//            text = "Currency Recognition";
//        }

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        speak(text);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onLongClick(View view) {
        Intent intent = null;
        if (view == msgBox) {
            intent = new Intent(MainActivity.this, messeging.class);
        } else if (view == phoneMngr) {
            intent = new Intent(MainActivity.this, phonemanager.class);
        } else if (view == timeDate) {
            intent = new Intent(MainActivity.this, datetime.class);
        }

        if (intent != null) {
            startActivity(intent);
        }

        return true; // Indicating event is consumed
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
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
            speak("Welcome to Third eye.The options available are Message Inbox, Phone Manager, and Battery Status. Message Inbox is on the top left, Phone Manager is on the top right, and Battery Status is on the bottom left. Long press to open.");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        speak("You are in the home page.");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
