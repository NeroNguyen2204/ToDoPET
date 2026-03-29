package com.example.doanmon;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReminderHelper {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleNotification(Context context, long timeInMillis, String title, String desc, int taskId) {
        // Chỉ hẹn giờ nếu thời gian ở tương lai
        if (timeInMillis <= System.currentTimeMillis()) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("TASK_TITLE", title);
        intent.putExtra("TASK_DESC", desc);

        // Sử dụng FLAG_IMMUTABLE cho Android 12+
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Cho phép chạy ngay cả khi máy đang ở chế độ tiết kiệm pin (Doze mode)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
                Log.d("ReminderHelper", "Đã hẹn giờ thông báo cho: " + title + " lúc " + timeInMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
