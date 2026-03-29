package com.example.doanmon;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ShareHelper {

    private Context context;

    public ShareHelper(Context context) {
        this.context = context;
    }

    public void showShareBottomSheet(Task taskToShare) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_share, null);
        bottomSheetDialog.setContentView(sheetView);

        LinearLayout btnShareZalo = sheetView.findViewById(R.id.btn_share_zalo);
        LinearLayout btnShareMessenger = sheetView.findViewById(R.id.btn_share_messenger);

        // Gộp mô tả và ngày hạn chót
        String fullDetails = taskToShare.description + "\nHạn chót: " + taskToShare.date;

        btnShareZalo.setOnClickListener(v -> {
            shareViaZalo(taskToShare.title, fullDetails);
            bottomSheetDialog.dismiss();
        });

        btnShareMessenger.setOnClickListener(v -> {
            shareViaMessenger(taskToShare.title, fullDetails);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void shareViaZalo(String title, String details) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Nhiệm vụ: " + title + "\nChi tiết: " + details);
        intent.setPackage("com.zing.zalo");

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Máy bạn chưa cài đặt Zalo!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViaMessenger(String title, String details) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Nhiệm vụ: " + title + "\nChi tiết: " + details);
        intent.setPackage("com.facebook.orca");

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Máy bạn chưa cài đặt Messenger!", Toast.LENGTH_SHORT).show();
        }
    }
}
