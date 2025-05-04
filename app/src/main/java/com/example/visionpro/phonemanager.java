package com.example.visionpro;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class phonemanager extends AppCompatActivity {

    private EditText editTextPhone;
    private ImageView voiceInput;
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_VOICE_INPUT = 2;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonemanager);

        editTextPhone = findViewById(R.id.editTextPhone2);
        voiceInput = findViewById(R.id.imageView2);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnBack = findViewById(R.id.btnBack);

        // Initialize Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("en", "IN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(new Locale("kn", "IN")); // Kannada
                }
                tts.setSpeechRate(1.0f);
                speak("You are in the phone manager app. Tap on top to speak a mobile number, and at the bottom to connect the call.");
            }
        });

        // Set click listener for voice input
        voiceInput.setOnClickListener(view -> {
            speak("say number.");
            startVoiceRecognition();
        });

        // Back button to delete last digit
        btnBack.setOnClickListener(view -> {
            String currentText = editTextPhone.getText().toString();
            if (!currentText.isEmpty()) {
                editTextPhone.setText(currentText.substring(0, currentText.length() - 1));
            }
        });

        // Call button functionality
        btnCall.setOnClickListener(view -> makePhoneCall());
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQUEST_VOICE_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice input is not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String phoneNumber = result.get(0).replaceAll("[^0-9]", ""); // Extract digits only
                if (!phoneNumber.isEmpty()) {
                    editTextPhone.setText(phoneNumber);
                    speakDigits(phoneNumber); // Speak each digit separately
                } else {
                    speak("Say the number again.");
                    startVoiceRecognition();
                }
            }
        }
    }

    private void speakDigits(String phoneNumber) {
        StringBuilder spokenDigits = new StringBuilder();
        for (char digit : phoneNumber.toCharArray()) {
            spokenDigits.append(digit).append(" "); // Add space between digits
        }
        speak("The phone number you provided is " + spokenDigits.toString().trim());
    }

    private void makePhoneCall() {
        String phoneNumber = editTextPhone.getText().toString();
        if (!phoneNumber.isEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        } else {
            speak("Please enter or say a phone number first.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                speak("Permission denied. Cannot make a call.");
            }
        }
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
}
