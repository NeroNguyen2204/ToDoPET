package com.example.doanmon;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TODO_REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Lấy dữ liệu tên công việc từ ReminderHelper truyền sang
        String title = intent.getStringExtra("TASK_TITLE");
        String desc = intent.getStringExtra("TASK_DESC");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Từ Android 8.0 trở lên bắt buộc phải tạo Notification Channel (Kênh thông báo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo công việc",
                    NotificationManager.IMPORTANCE_HIGH // Mức độ ưu tiên cao để thông báo rớt xuống
            );
            channel.setDescription("Kênh nhắc nhở các công việc đến hạn");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Tạo hành động: Khi người dùng bấm vào thông báo thì mở MainActivity lên lại
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tiến hành "vẽ" giao diện thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder) // Icon mặc định của Android
                .setContentTitle(title != null ? "Đến hạn: " + title : "Nhắc nhở công việc")
                .setContentText(desc != null ? desc : "Đã đến hạn chót công việc của bạn!")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Hiển thị đè lên màn hình
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Kèm âm thanh rung mặc định
                .setContentIntent(pendingIntent) // Gắn hành động bấm vào mở app
                .setAutoCancel(true); // Bấm vào xong tự tắt thông báo

        // Đẩy thông báo lên màn hình (Dùng thời gian hiện tại làm ID để các thông báo không đè lên nhau)
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}