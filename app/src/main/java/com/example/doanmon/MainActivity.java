package com.example.doanmon;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    RecyclerView rvTasks;
    TaskAdapter adapter;
    List<Task> taskList;
    TaskDatabase db; // MCao sửa

    SearchView searchView;
    Spinner spFilter;
    FloatingActionButton fabAdd;

    String selectedDateTime = ""; // Biến lưu tạm thời gian Tú Văn chọn

    android.content.SharedPreferences sharedPreferences;
    int currentExp = 0;
    int currentLevel = 1;
    android.widget.TextView tvLevelStatus;
    android.widget.ProgressBar pbExp;
    android.widget.ImageView imgPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        tvLevelStatus = findViewById(R.id.tvLevelStatus);
        pbExp = findViewById(R.id.pbExp);
        imgPet = findViewById(R.id.imgPet);

        sharedPreferences = getSharedPreferences("RPG_DATA", MODE_PRIVATE);

        rvTasks = findViewById(R.id.rvTasks);
        searchView = findViewById(R.id.searchView);
        spFilter = findViewById(R.id.spFilter);
        fabAdd = findViewById(R.id.fabAdd);

        taskList = new ArrayList<>();
        db = TaskDatabase.getInstance(this);
        taskList = db.taskDao().getAllTasks();

        adapter = new TaskAdapter(this, taskList);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddOptionsBottomSheet());

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                Task taskToDelete = taskList.get(pos);
                db.taskDao().deleteTask(taskToDelete);
                taskList.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rvTasks);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Task> filtered = new ArrayList<>();
                for (Task task : taskList) {
                    if (task.title.toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(task);
                    }
                }
                adapter = new TaskAdapter(MainActivity.this, filtered);
                rvTasks.setAdapter(adapter);
                return true;
            }
        });

        String[] filterOptions = {"Tất cả", "Đã hoàn thành","Chưa hoàn thành"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filterOptions);
        spFilter.setAdapter(filterAdapter);

        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    adapter = new TaskAdapter(MainActivity.this, taskList);
                } else if (position == 1) {
                    List<Task> completed = new ArrayList<>();
                    for (Task t : taskList) {
                        if (t.isCompleted) completed.add(t);
                    }
                    adapter = new TaskAdapter(MainActivity.this, completed);
                } else if (position == 2) {
                    List<Task> incomplete = new ArrayList<>();
                    for (Task t : taskList) {
                        if (!t.isCompleted) incomplete.add(t);
                    }
                    adapter = new TaskAdapter(MainActivity.this, incomplete);
                }
                rvTasks.setAdapter(adapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        loadRpgData();
        updateUI_RPG();
    }

    private void showAddOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.layout_add_options, null);
        bottomSheetDialog.setContentView(sheetView);

        LinearLayout btnAddTask = sheetView.findViewById(R.id.btn_option_add_task);
        LinearLayout btnFocus = sheetView.findViewById(R.id.btn_option_focus);

        btnAddTask.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showAddTaskDialog();
        });

        btnFocus.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, FocusActivity.class);
            startActivity(intent);
        });

        bottomSheetDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRpgData(); // Quan trọng: Nạp lại dữ liệu mới nhất từ SharedPreferences
        updateUI_RPG();
    }

    private void loadRpgData() {
        currentExp = sharedPreferences.getInt("EXP", 0);
        currentLevel = sharedPreferences.getInt("LEVEL", 1);
    }

    public void addExpBasedOnPriority(String priority) {
        int points = priority.equals("HIGH") ? 50 : (priority.equals("MEDIUM") ? 30 : 10);
        currentExp += points;
        checkLevelUp();
    }

    public void removeExpBasedOnPriority(String priority) {
        int points = priority.equals("HIGH") ? 50 : (priority.equals("MEDIUM") ? 30 : 10);
        currentExp -= points;
        if (currentExp < 0) currentExp = 0;
        saveRpgData();
    }

    private void checkLevelUp() {
        int expNeeded = currentLevel * 100;
        if (currentExp >= expNeeded) {
            currentLevel++;
            currentExp -= expNeeded;
            Toast.makeText(this, "🎉 LÊN CẤP " + currentLevel + "!", Toast.LENGTH_LONG).show();
        }
        saveRpgData();
    }

    private void saveRpgData() {
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("EXP", currentExp);
        editor.putInt("LEVEL", currentLevel);
        editor.apply();
        updateUI_RPG();
    }

    public void updateUI_RPG() {
        int expNeeded = currentLevel * 100;
        tvLevelStatus.setText("Cấp độ: " + currentLevel + " | Điểm: " + currentExp + "/" + expNeeded);
        pbExp.setMax(expNeeded);
        pbExp.setProgress(currentExp);

        boolean hasOverdueTask = false;
        long currentTime = System.currentTimeMillis();

        if (taskList != null) {
            for (Task t : taskList) {
                if (!t.isCompleted) {
                    long deadline = parseTimeToMillis(t.date);
                    if (deadline > 0 && deadline < currentTime) {
                        hasOverdueTask = true;
                        break;
                    }
                }
            }
        }

        if (hasOverdueTask) {
            imgPet.setImageResource(R.drawable.ic_pet_sad);
        } else {
            imgPet.setImageResource(R.drawable.ic_pet_happy);
        }
    }

    private long parseTimeToMillis(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - d/M/yyyy", java.util.Locale.ENGLISH);
            java.util.Date date = sdf.parse(dateTimeStr);
            if (date != null) return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        Button btnPickDateTime = dialogView.findViewById(R.id.btnPickDateTime);
        Spinner spPriorityInput = dialogView.findViewById(R.id.spPriorityInput);

        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spPriorityInput.setAdapter(priorityAdapter);

        selectedDateTime = "";

        btnPickDateTime.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("CHỌN NGÀY HẠN CHÓT")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                String date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("CHỌN GIỜ HẠN CHÓT")
                        .build();

                timePicker.addOnPositiveButtonClickListener(v1 -> {
                    String time = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                    selectedDateTime = time + " - " + date;
                    btnPickDateTime.setText(selectedDateTime);
                });
                timePicker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
            });
            datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        AlertDialog dialog = builder.setTitle("Thêm Công Việc Mới")
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String title = etTaskTitle.getText().toString().trim();
                String description = etTaskDescription.getText().toString().trim();
                String priority = spPriorityInput.getSelectedItem().toString();

                if (title.isEmpty()) {
                    etTaskTitle.setError("Vui lòng nhập tiêu đề!");
                    etTaskTitle.requestFocus();
                    return;
                }

                if (selectedDateTime.isEmpty()) {
                    selectedDateTime = "Chưa đặt hạn chót";
                }

                Task newTask = new Task(title, description, selectedDateTime, priority, false);
                db.taskDao().insertTask(newTask);
                
                long timeInMillis = parseTimeToMillis(selectedDateTime);
                if (timeInMillis > System.currentTimeMillis()) {
                    int randomTaskId = (int) System.currentTimeMillis();
                    ReminderHelper.scheduleNotification(MainActivity.this, timeInMillis, title, "Đã đến hạn chót công việc!", randomTaskId);
                }
                
                taskList.clear();
                taskList.addAll(db.taskDao().getAllTasks());
                adapter.notifyDataSetChanged();
                rvTasks.scrollToPosition(0);
                updateUI_RPG();

                Toast.makeText(MainActivity.this, "Đã thêm công việc!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    public void showEditTaskDialog(Task taskToEdit, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        Button btnPickDateTime = dialogView.findViewById(R.id.btnPickDateTime);
        Spinner spPriorityInput = dialogView.findViewById(R.id.spPriorityInput);

        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spPriorityInput.setAdapter(priorityAdapter);

        etTaskTitle.setText(taskToEdit.title);
        etTaskDescription.setText(taskToEdit.description);
        btnPickDateTime.setText(taskToEdit.date);
        selectedDateTime = taskToEdit.date;

        if (taskToEdit.priority.equals("HIGH")) spPriorityInput.setSelection(0);
        else if (taskToEdit.priority.equals("MEDIUM")) spPriorityInput.setSelection(1);
        else spPriorityInput.setSelection(2);

        btnPickDateTime.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("CHỌN NGÀY HẠN CHÓT")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                String date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("CHỌN GIỜ HẠN CHÓT")
                        .build();

                timePicker.addOnPositiveButtonClickListener(v1 -> {
                    String time = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                    selectedDateTime = time + " - " + date;
                    btnPickDateTime.setText(selectedDateTime);
                });
                timePicker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
            });
            datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        AlertDialog dialog = builder.setTitle("Sửa Ghi Chú")
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String title = etTaskTitle.getText().toString().trim();
                String description = etTaskDescription.getText().toString().trim();
                String priority = spPriorityInput.getSelectedItem().toString();

                if (title.isEmpty()) {
                    etTaskTitle.setError("Vui lòng nhập tiêu đề!");
                    etTaskTitle.requestFocus();
                    return;
                }

                taskToEdit.title = title;
                taskToEdit.description = description;
                taskToEdit.date = selectedDateTime;
                taskToEdit.priority = priority;

                db.taskDao().updateTask(taskToEdit);
                long timeInMillis = parseTimeToMillis(selectedDateTime);
                if (timeInMillis > System.currentTimeMillis()) {
                    ReminderHelper.scheduleNotification(MainActivity.this, timeInMillis, taskToEdit.title, description.isEmpty() ? "Đã đến hạn chót công việc!" : description, taskToEdit.id);
                }
                adapter.notifyItemChanged(position);
                updateUI_RPG();
                Toast.makeText(MainActivity.this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }
    public void updateTaskInDb(Task task) {
        db.taskDao().updateTask(task);
    }
}
