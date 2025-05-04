package com.example.visionpro;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class datetime extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private static final String TAG = "SPEECH";
    private TextToSpeech tts;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datetime);

        tts = new TextToSpeech(this, this);
        findViewById(R.id.batteryCard).setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.batteryCard) {
            TextView currDate = findViewById(R.id.currDateTime);
            Date date = getCurrentDateTime();
            tts.setSpeechRate(0.8f);
            String dateInString = toString(date, "E, dd MMMM yyyy HH:mm:ss");
            currDate.setText(dateInString);
            tts.speak(dateInString, TextToSpeech.QUEUE_FLUSH, null, null);

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);

            int status = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
            String isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ? "Phone is charging" : "Phone is not charging";

            TextView txtView = findViewById(R.id.batteryStatus);
            txtView.setText(isCharging);

            float batteryPct = 0;
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPct = (level * 100) / (float) scale;
            }

            TextView txtBattery = findViewById(R.id.batteryPer);
            txtBattery.setText(String.valueOf(batteryPct));
            tts.setSpeechRate(0.8f);
            String bst = "Your battery level is " + batteryPct + " percent and " + isCharging;
            tts.speak(bst, TextToSpeech.QUEUE_FLUSH, null, null);
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
        Log.d(TAG, "Initializing TTS");
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "SUCCESS");
//            tts.setLanguage(Locale.US);
            int result = tts.setLanguage(new Locale("en", "IN"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(new Locale("kn", "IN")); // Kannada
            }
            tts.setSpeechRate(1.0f);
            tts.speak("Time, date, and battery status opened. click on middle to know information", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private String toString(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(date);
    }

    private Date getCurrentDateTime() {
        return Calendar.getInstance().getTime();
    }
}
