package com.example.smartmedicine.ui;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpcomingMedicineAdapter extends RecyclerView.Adapter<UpcomingMedicineAdapter.UpcomingViewHolder> {

    private List<MedicineInfo> medicineList = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    public UpcomingMedicineAdapter() {
        startCountdownUpdate();
    }

    public void setMedicineList(List<MedicineInfo> list) {
        this.medicineList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    private void startCountdownUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    public void onDestroy() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    @NonNull
    @Override
    public UpcomingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_medicine, parent, false);
        return new UpcomingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingViewHolder holder, int position) {
        MedicineInfo info = medicineList.get(position);
        
        holder.tvName.setText(info.getMedicineName());
        holder.tvDosage.setText(info.getDosage() != null ? info.getDosage() : "");

        String imagePath = info.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                try {
                    android.graphics.Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        holder.ivMedicine.setImageBitmap(bitmap);
                        holder.ivMedicine.setColorFilter(null);
                        holder.ivMedicine.setPadding(0, 0, 0, 0);
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

        String countdown = getCountdownText(info.getUsageTime());
        holder.tvCountdown.setText(countdown);
    }

    private void setDefaultIcon(UpcomingViewHolder holder) {
        holder.ivMedicine.setImageResource(R.drawable.ic_medicine);
        holder.ivMedicine.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.primary));
        int padding = (int) (10 * holder.itemView.getResources().getDisplayMetrics().density);
        holder.ivMedicine.setPadding(padding, padding, padding, padding);
    }

    private String getCountdownText(String usageTime) {
        if (usageTime == null || usageTime.isEmpty()) {
            return "--:--:--";
        }

        try {
            String[] parts = usageTime.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0].trim());
                int minute = Integer.parseInt(parts[1].trim());

                Calendar now = Calendar.getInstance();
                Calendar target = (Calendar) now.clone();
                target.set(Calendar.HOUR_OF_DAY, hour);
                target.set(Calendar.MINUTE, minute);
                target.set(Calendar.SECOND, 0);

                if (target.getTimeInMillis() <= now.getTimeInMillis()) {
                    target.add(Calendar.DAY_OF_MONTH, 1);
                }

                long diff = target.getTimeInMillis() - now.getTimeInMillis();
                if (diff < 0) {
                    return "已过期";
                }

                long hours = diff / (1000 * 60 * 60);
                long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
                long seconds = (diff % (1000 * 60)) / 1000;

                return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            }
        } catch (Exception e) {
            return "--:--:--";
        }
        return "--:--:--";
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class UpcomingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedicine;
        TextView tvName;
        TextView tvDosage;
        TextView tvCountdown;

        UpcomingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedicine = itemView.findViewById(R.id.iv_medicine);
            tvName = itemView.findViewById(R.id.tv_medicine_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvCountdown = itemView.findViewById(R.id.tv_countdown);
        }
    }
}
