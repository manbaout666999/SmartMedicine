package com.example.smartmedicine.reminder;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * 语音播报工具（基于 TextToSpeech）
 */
public class VoiceAlarmService {

    private static TextToSpeech textToSpeech;
    private static boolean isInit = false;

    public static void speak(Context context, String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // 不支持中文时退回系统默认语言
                        textToSpeech.setLanguage(Locale.getDefault());
                    }
                    isInit = true;
                    textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, "medicine_alarm");
                }
            });
        } else {
            if (isInit) {
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, "medicine_alarm");
            }
        }
    }
}

