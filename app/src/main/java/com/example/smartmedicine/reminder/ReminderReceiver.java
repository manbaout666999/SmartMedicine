package com.example.smartmedicine.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * 闹钟广播接收器：收到闹钟后弹出提示并进行语音播报
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicineName");
        String dosageText = intent.getStringExtra("dosageText");

        String text = "请按时服用 " + medicineName + "，" + (dosageText != null ? dosageText : "");
        Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG).show();

        VoiceAlarmService.speak(context.getApplicationContext(), text);
    }
}

