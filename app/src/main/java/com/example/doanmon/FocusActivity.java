package com.example.doanmon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    private EditText etMinutes;
    private Button btnStartFocus, btnExitFocus;
    private TextView tvCountdown;
    private ProgressBar pbFocus;
    private LinearLayout layoutTimerPicker, layoutCountdown;

    private CountDownTimer countDownTimer;
    private boolean isFocusing = false;
    private long timeLeftInMillis;
    private int initialMinutes = 0;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_focus);

        sharedPreferences = getSharedPreferences("RPG_DATA", MODE_PRIVATE);

        etMinutes = findViewById(R.id.etMinutes);
        btnStartFocus = findViewById(R.id.btnStartFocus);
        btnExitFocus = findViewById(R.id.btnExitFocus);
        tvCountdown = findViewById(R.id.tvCountdown);
        pbFocus = findViewById(R.id.pbFocus);
        layoutTimerPicker = findViewById(R.id.layoutTimerPicker);
        layoutCountdown = findViewById(R.id.layoutCountdown);

        btnStartFocus.setOnClickListener(v -> startFocus());
        btnExitFocus.setOnClickListener(v -> showExitConfirmation());
    }

    private void startFocus() {
        String input = etMinutes.getText().toString();
        if (input.isEmpty() || Integer.parseInt(input) <= 0) {
            Toast.makeText(this, "Vui lòng nhập số phút!", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        initialMinutes = Integer.parseInt(input);
        timeLeftInMillis = initialMinutes * 60000L;

        layoutTimerPicker.setVisibility(View.GONE);
        layoutCountdown.setVisibility(View.VISIBLE);
        pbFocus.setMax((int) (timeLeftInMillis / 1000));
        pbFocus.setProgress((int) (timeLeftInMillis / 1000));

        isFocusing = true;
        try {
            startLockTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startCountDown();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void startCountDown() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                pbFocus.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isFocusing = false;
                int gainedExp = initialMinutes * 2;
                updateExp(gainedExp);
                
                tvCountdown.setText("Hoàn thành!");
                Toast.makeText(FocusActivity.this, "Tuyệt vời! Bạn nhận được " + gainedExp + " XP.", Toast.LENGTH_LONG).show();
                
                stopLockTaskSafe();
                finish();
            }
        }.start();
    }

    private void updateExp(int amount) {
        int currentExp = sharedPreferences.getInt("EXP", 0);
        int currentLevel = sharedPreferences.getInt("LEVEL", 1);
        
        currentExp += amount;
        
        while (currentExp >= currentLevel * 100) {
            currentExp -= currentLevel * 100;
            currentLevel++;
            Toast.makeText(this, "🎉 LÊN CẤP " + currentLevel + "!", Toast.LENGTH_SHORT).show();
        }
        
        if (currentExp < 0) currentExp = 0;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("EXP", currentExp);
        editor.putInt("LEVEL", currentLevel);
        editor.apply();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvCountdown.setText(timeFormatted);
    }

    private void showExitConfirmation() {
        int penaltyExp = initialMinutes * 1;
        new AlertDialog.Builder(this)
                .setTitle("Thoát chế độ tập trung?")
                .setMessage("Nếu thoát ngay bây giờ, bạn sẽ bị trừ " + penaltyExp + " XP. Bạn chắc chắn chứ?")
                .setPositiveButton("Thoát (Bị trừ XP)", (dialog, which) -> {
                    isFocusing = false; // Đánh dấu là đã thoát chủ động
                    if (countDownTimer != null) countDownTimer.cancel();
                    updateExp(-penaltyExp);
                    Toast.makeText(this, "Bạn đã bị trừ " + penaltyExp + " XP do bỏ dở tập trung.", Toast.LENGTH_SHORT).show();
                    stopLockTaskSafe();
                    finish();
                })
                .setNegativeButton("Tiếp tục tập trung", null)
                .show();
    }

    private void stopLockTaskSafe() {
        try {
            stopLockTask();
        } catch (Exception e) {
            // Đã dừng hoặc không trong chế độ ghim
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        // Nếu người dùng bằng cách nào đó thoát ra ngoài (Home/Swipe) khi đang tập trung
        if (isFocusing) {
            isFocusing = false;
            if (countDownTimer != null) countDownTimer.cancel();
            updateExp(-(initialMinutes * 1)); // Phạt XP
            stopLockTaskSafe(); // Gỡ ghim
            finish(); // Đóng Activity để khi vào lại sẽ bắt đầu mới
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
