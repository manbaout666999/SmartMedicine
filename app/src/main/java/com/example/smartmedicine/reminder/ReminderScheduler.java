package com.example.smartmedicine.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * 用药闹钟调度工具
 */
public class ReminderScheduler {

    /**
     * 根据提醒信息设置一次性闹钟
     */
    public static void scheduleReminder(Context context, MedicineReminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medicineName", reminder.medicineName);
        intent.putExtra("dosageText", reminder.dosageText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour);
        calendar.set(Calendar.MINUTE, reminder.minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerAtMillis = calendar.getTimeInMillis();
        if (triggerAtMillis <= System.currentTimeMillis()) {
            // 如果时间已过，则顺延到下一天
            triggerAtMillis += 24 * 60 * 60 * 1000L;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // 检查是否可以使用精确闹钟
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerAtMillis,
                                pendingIntent
                        );
                    } else {
                        // 使用非精确闹钟作为后备
                        alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerAtMillis,
                                pendingIntent
                        );
                    }
                } else {
                    // Android 12以下可以直接使用精确闹钟
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                }
            } catch (Exception e) {
                // 如果出现异常，使用最安全的方法
                try {
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

