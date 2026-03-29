package com.example.doanmon;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView; // Nhớ thêm dòng Import này nhé
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    Context context;
    List<Task> taskList;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        Task task = taskList.get(position);

        holder.tvTitle.setText(task.title);
        holder.tvDeadline.setText(task.date); // Hiển thị ngày giờ
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(task.isCompleted);

        // Đổi màu thanh đánh dấu mức độ ưu tiên
        View viewPriority = holder.itemView.findViewById(R.id.viewPriority);
        if (task.priority.equals("HIGH")) {
            viewPriority.setBackgroundColor(Color.parseColor("#F44336")); // Đỏ
        } else if (task.priority.equals("MEDIUM")) {
            viewPriority.setBackgroundColor(Color.parseColor("#FFC107")); // Vàng
        } else {
            viewPriority.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh lá
        }



        // Lắng nghe sự kiện tick CheckBox để cập nhật trạng thái và tính điểm
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isCompleted = isChecked;

            if (context instanceof MainActivity) {
                ((MainActivity) context).updateTaskInDb(task); // Lưu vào DB

                // --- Xử lý cộng/trừ điểm EXP ---
                if (isChecked) {
                    ((MainActivity) context).addExpBasedOnPriority(task.priority);
                } else {
                    ((MainActivity) context).removeExpBasedOnPriority(task.priority);
                }
            }
        });
        // Sự kiện click vào cả một dòng công việc để mở form sửa
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).showEditTaskDialog(task, position);
            }
        });

        // ========== CODE MỚI THÊM: SỰ KIỆN CLICK NÚT CHIA SẺ ==========
        holder.btnShareTask.setOnClickListener(v -> {
            // Gọi ShareHelper để hiển thị BottomSheet (Bảng trượt Zalo/Messenger)
            ShareHelper shareHelper = new ShareHelper(context);
            shareHelper.showShareBottomSheet(task);
        });
        // ==============================================================
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDeadline;
        CheckBox cbDone;
        ImageView btnShareTask; // KHAI BÁO THÊM NÚT CHIA SẺ

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            cbDone = itemView.findViewById(R.id.cbDone);
            btnShareTask = itemView.findViewById(R.id.btnShareTask); // ÁNH XẠ NÚT CHIA SẺ
        }
    }
}