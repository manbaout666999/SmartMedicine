package com.example.smartmedicine.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmedicine.R;
import com.example.smartmedicine.base.MedicineInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 药物列表适配器：展示药名、剂量、下次服用时间等基础信息
 */
public class MedicineListAdapter extends RecyclerView.Adapter<MedicineListAdapter.MedicineViewHolder> {

    public interface OnMedicineActionListener {
        void onEditMedicine(int position);
        void onDeleteMedicine(int position);
    }

    private final List<MedicineInfo> originList;
    private final List<MedicineInfo> displayList = new ArrayList<>();
    private OnMedicineActionListener listener;

    public MedicineListAdapter(List<MedicineInfo> data) {
        this.originList = data;
        this.displayList.addAll(data);
    }

    public void setOnMedicineActionListener(OnMedicineActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineInfo info = displayList.get(position);
        holder.tvName.setText(info.getMedicineName());
        holder.tvSpec.setText(info.getUsageMethod() != null ? info.getUsageMethod() : "");
        holder.tvStock.setText(info.getDosage() != null ? info.getDosage() : "");
        holder.tvNextTime.setText("下次服用: " + (info.getUsageTime() != null ? info.getUsageTime() : "--:--"));
        holder.tvTime1.setText(info.getUsageTime() != null ? info.getUsageTime() : "--:--");

        // 加载药品图片
        String imagePath = info.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        holder.ivMedicineIcon.setImageBitmap(bitmap);
                        holder.ivMedicineIcon.setColorFilter(null);
                        holder.ivMedicineIcon.setPadding(0, 0, 0, 0);
                    } else {
                        setDefaultIcon(holder);
                    }
                } catch (Exception e) {
                    setDefaultIcon(holder);
                }
            } else {
                setDefaultIcon(holder);
            }
        } else {
            setDefaultIcon(holder);
        }

        // 设置点击事件
        final int pos = position;
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMedicine(pos);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMedicine(pos);
            }
        });
    }

    private void setDefaultIcon(MedicineViewHolder holder) {
        holder.ivMedicineIcon.setImageResource(R.drawable.ic_medicine);
        int primaryColor = holder.itemView.getContext().getResources().getColor(R.color.primary);
        holder.ivMedicineIcon.setColorFilter(primaryColor);
        int padding = (int) (16 * holder.itemView.getResources().getDisplayMetrics().density);
        holder.ivMedicineIcon.setPadding(padding, padding, padding, padding);
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public void filter(String keyword) {
        displayList.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            displayList.addAll(originList);
        } else {
            String lower = keyword.toLowerCase();
            for (MedicineInfo info : originList) {
                if (info.getMedicineName() != null &&
                        info.getMedicineName().toLowerCase().contains(lower)) {
                    displayList.add(info);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedicineIcon;
        ImageView ivEdit;
        ImageView ivDelete;
        TextView tvName;
        TextView tvSpec;
        TextView tvStock;
        TextView tvNextTime;
        TextView tvTime1;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedicineIcon = itemView.findViewById(R.id.iv_medicine_icon);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvName = itemView.findViewById(R.id.tv_medicine_name);
            tvSpec = itemView.findViewById(R.id.tv_spec);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvNextTime = itemView.findViewById(R.id.tv_next_time);
            tvTime1 = itemView.findViewById(R.id.tv_time_1);
        }
    }
}

