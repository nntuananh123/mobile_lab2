package com.example.speech_to_text_translator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 100;

    private Spinner sourceLanguageSpinner;
    private Spinner targetLanguageSpinner;
    private TextView originalTextView;
    private TextView translatedTextView;
    private Button recordButton;

    private Map<String, String> languageCodes;
    private HashMap<String, Translator> translators = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner);
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner);
        originalTextView = findViewById(R.id.originalTextView);
        translatedTextView = findViewById(R.id.translatedTextView);
        recordButton = findViewById(R.id.recordButton);

        // Setup language options
        setupLanguages();

        // Set button click listener
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });
    }

    private void setupLanguages() {
        // Initialize language codes map (for translation API)
        languageCodes = new HashMap<>();
        languageCodes.put("English", "en");
        languageCodes.put("Vietnamese", "vi");
        languageCodes.put("Spanish", "es");
        languageCodes.put("French", "fr");
        languageCodes.put("German", "de");
        languageCodes.put("Italian", "it");
        languageCodes.put("Japanese", "ja");
        languageCodes.put("Chinese", "zh");
        languageCodes.put("Russian", "ru");
        languageCodes.put("Arabic", "ar");

        // Create array adapter for spinners
        ArrayList<String> languages = new ArrayList<>(languageCodes.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        sourceLanguageSpinner.setAdapter(adapter);
        targetLanguageSpinner.setAdapter(adapter);

        // Set default languages
        sourceLanguageSpinner.setSelection(languages.indexOf("English"));
        targetLanguageSpinner.setSelection(languages.indexOf("Vietnamese")); // Set Vietnamese as default target language
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Get the selected source language
        String sourceLanguage = sourceLanguageSpinner.getSelectedItem().toString();
        String languageCode = languageCodes.get(sourceLanguage);

        // If Vietnamese is selected, specifically use Vietnamese locale
        if (sourceLanguage.equals("Vietnamese")) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
        }

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String recognizedText = result.get(0);
            originalTextView.setText(recognizedText);

            // Call translation function
            translateText(recognizedText);
        }
    }

    private void translateText(String textToTranslate) {
        // Show loading indicator
        translatedTextView.setText("Translating...");

        // Get source and target languages
        String sourceLanguage = sourceLanguageSpinner.getSelectedItem().toString();
        String targetLanguage = targetLanguageSpinner.getSelectedItem().toString();

        String sourceCode = languageCodes.get(sourceLanguage);
        String targetCode = languageCodes.get(targetLanguage);

        // Create a translator key
        String translatorKey = sourceCode + "-" + targetCode;

        // Check if we already have this translator
        if (translators.containsKey(translatorKey)) {
            // Use existing translator
            performTranslation(translators.get(translatorKey), textToTranslate);
        } else {
            // Create a new translator
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(getMLKitLanguageCode(sourceCode))
                    .setTargetLanguage(getMLKitLanguageCode(targetCode))
                    .build();

            Translator translator = Translation.getClient(options);

            // Create download conditions with wifi requirement turned off
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()  // Change to false if you want downloads without WiFi
                    .build();

            // Download translation model if needed
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(unused -> {
                        // Model downloaded successfully, store for reuse
                        translators.put(translatorKey, translator);
                        performTranslation(translator, textToTranslate);
                    })
                    .addOnFailureListener(e -> {
                        translatedTextView.setText("Error downloading model: " + e.getMessage());
                        e.printStackTrace();
                    });
        }
    }

    // Helper method to perform translation with a ready translator
    private void performTranslation(Translator translator, String text) {
        translator.translate(text)
                .addOnSuccessListener(translatedText -> {
                    translatedTextView.setText(translatedText);
                })
                .addOnFailureListener(e -> {
                    translatedTextView.setText("Translation error: " + e.getMessage());
                    e.printStackTrace();
                });
    }

    // Helper method to convert ISO language codes to ML Kit language codes
    private String getMLKitLanguageCode(String isoCode) {
        // ML Kit uses specific constants for language codes
        switch (isoCode) {
            case "en": return TranslateLanguage.ENGLISH;
            case "vi": return TranslateLanguage.VIETNAMESE;
            case "es": return TranslateLanguage.SPANISH;
            case "fr": return TranslateLanguage.FRENCH;
            case "de": return TranslateLanguage.GERMAN;
            case "it": return TranslateLanguage.ITALIAN;
            case "ja": return TranslateLanguage.JAPANESE;
            case "zh": return TranslateLanguage.CHINESE;
            case "ru": return TranslateLanguage.RUSSIAN;
            case "ar": return TranslateLanguage.ARABIC;
            default: return TranslateLanguage.ENGLISH;
        }
    }

    // Free up resources when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close all translators to free up resources
        for (Translator translator : translators.values()) {
            translator.close();
        }
    }
}