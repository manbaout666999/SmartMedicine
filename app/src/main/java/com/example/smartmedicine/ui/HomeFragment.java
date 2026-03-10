package com.example.smartmedicine.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmedicine.R;
import com.example.smartmedicine.MainActivity;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 首页Fragment - 显示今日用药和快捷操作
 */
public class HomeFragment extends Fragment {

    private TextView tvDate;
    private TextView tvMedicineCount;
    private TextView tvMedicineHint;
    private RecyclerView rvTodayMedicine;
    private TextView tvNoMedicineToday;
    private MaterialCardView cardAddMedicine;
    private MaterialCardView cardScan;
    private RecyclerView rvUpcoming;
    private TextView tvViewAll;
    
    private UpcomingMedicineAdapter upcomingAdapter;
    private android.os.Handler updateHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable updateRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initData();
        setupListeners();
    }

    private void initViews(View view) {
        tvDate = view.findViewById(R.id.tv_date);
        tvMedicineCount = view.findViewById(R.id.tv_medicine_count);
        tvMedicineHint = view.findViewById(R.id.tv_medicine_hint);
        rvTodayMedicine = view.findViewById(R.id.rv_today_medicine);
        tvNoMedicineToday = view.findViewById(R.id.tv_no_medicine_today);
        cardAddMedicine = view.findViewById(R.id.card_add_medicine);
        cardScan = view.findViewById(R.id.card_scan);
        rvUpcoming = view.findViewById(R.id.rv_upcoming);
        tvViewAll = view.findViewById(R.id.tv_view_all);
    }

    private void initData() {
        // 设置当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        // 设置今日用药数量（从存储中获取）
        updateMedicineCount();

        // 设置RecyclerView
        rvTodayMedicine.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 设置即将用药适配器
        upcomingAdapter = new UpcomingMedicineAdapter();
        rvUpcoming.setAdapter(upcomingAdapter);
        
        // 更新即将用药列表
        updateUpcomingMedicineList();
        
        // 开始定时更新
        startPeriodicUpdate();
    }

    private void updateMedicineCount() {
        // 从MedicineStorage获取药品数量
        if (getContext() != null) {
            int count = com.example.smartmedicine.util.MedicineStorage.loadMedicineList(getContext()).size();
            if (count > 0) {
                tvMedicineCount.setText(count + " 种");
                tvMedicineHint.setText("记得按时服药哦！");
            } else {
                tvMedicineCount.setText("0 种");
                tvMedicineHint.setText("快去添加一些药物吧！");
            }
        }
    }

    private void updateUpcomingMedicineList() {
        if (getContext() == null) return;
        
        List<com.example.smartmedicine.base.MedicineInfo> allMedicines = 
            com.example.smartmedicine.util.MedicineStorage.loadMedicineList(getContext());
        
        List<com.example.smartmedicine.base.MedicineInfo> upcomingMedicines = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        Calendar twelveHoursLater = (Calendar) now.clone();
        twelveHoursLater.add(Calendar.HOUR, 12);
        
        for (com.example.smartmedicine.base.MedicineInfo medicine : allMedicines) {
            String usageTime = medicine.getUsageTime();
            if (usageTime != null && !usageTime.isEmpty()) {
                try {
                    String[] parts = usageTime.split(":");
                    if (parts.length >= 2) {
                        int hour = Integer.parseInt(parts[0].trim());
                        int minute = Integer.parseInt(parts[1].trim());
                        
                        Calendar targetTime = (Calendar) now.clone();
                        targetTime.set(Calendar.HOUR_OF_DAY, hour);
                        targetTime.set(Calendar.MINUTE, minute);
                        targetTime.set(Calendar.SECOND, 0);
                        
                        if (targetTime.getTimeInMillis() <= now.getTimeInMillis()) {
                            targetTime.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        
                        if (targetTime.getTimeInMillis() <= twelveHoursLater.getTimeInMillis()) {
                            upcomingMedicines.add(medicine);
                        }
                    }
                } catch (Exception e) {
                    // 跳过解析失败的药品
                }
            }
        }
        
        // 按时间排序
        upcomingMedicines.sort((a, b) -> {
            long timeA = getTimeInMillis(a.getUsageTime());
            long timeB = getTimeInMillis(b.getUsageTime());
            return Long.compare(timeA, timeB);
        });
        
        upcomingAdapter.setMedicineList(upcomingMedicines);
    }

    private long getTimeInMillis(String usageTime) {
        if (usageTime == null || usageTime.isEmpty()) return Long.MAX_VALUE;
        
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
                return target.getTimeInMillis();
            }
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
        return Long.MAX_VALUE;
    }

    private void startPeriodicUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateUpcomingMedicineList();
                updateHandler.postDelayed(this, 60000); // 每分钟更新一次
            }
        };
        updateHandler.post(updateRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMedicineCount();
        updateUpcomingMedicineList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
        if (upcomingAdapter != null) {
            upcomingAdapter.onDestroy();
        }
    }

    private void setupListeners() {
        // 添加药物点击事件
        cardAddMedicine.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_medicine);
            }
        });

        // 扫码识别点击事件
        cardScan.setOnClickListener(v -> {
            // 暂时跳转到药物页，由药物页负责拍照识药
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_medicine);
            }
        });

        // 查看全部点击事件
        tvViewAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_medicine);
            }
        });
    }
}
