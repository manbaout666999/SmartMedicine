package com.example.smartmedicine.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartmedicine.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * 健康数据Fragment
 */
public class HealthFragment extends Fragment {

    private TextView tvLastUpdate;
    private MaterialCardView cardQuickRecord;
    private TextView tvBloodPressure;
    private TextView tvBpStatus;
    private TextView tvHeartRate;
    private TextView tvHrStatus;
    private TextView tvBloodSugar;
    private TextView tvBsStatus;
    private MaterialButton btnTrend;
    private MaterialButton btnReport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initData();
        setupListeners();
    }

    private void initViews(View view) {
        tvLastUpdate = view.findViewById(R.id.tv_last_update);
        cardQuickRecord = view.findViewById(R.id.card_quick_record);
        tvBloodPressure = view.findViewById(R.id.tv_blood_pressure);
        tvBpStatus = view.findViewById(R.id.tv_bp_status);
        tvHeartRate = view.findViewById(R.id.tv_heart_rate);
        tvHrStatus = view.findViewById(R.id.tv_hr_status);
        tvBloodSugar = view.findViewById(R.id.tv_blood_sugar);
        tvBsStatus = view.findViewById(R.id.tv_bs_status);
        btnTrend = view.findViewById(R.id.btn_trend);
        btnReport = view.findViewById(R.id.btn_report);
    }

    private void initData() {
        // 设置最后更新时间
        tvLastUpdate.setText("最近更新：2024-01-15 10:30");

        // 设置健康数据（示例）
        tvBloodPressure.setText("120/80 mmHg");
        tvBpStatus.setText("正常");
        tvHeartRate.setText("75 次/分");
        tvHrStatus.setText("正常");
        tvBloodSugar.setText("5.6 mmol/L");
        tvBsStatus.setText("正常");
    }

    private void setupListeners() {
        // 快捷记录点击事件
        cardQuickRecord.setOnClickListener(v -> {
            // 跳转到记录健康数据页面
        });

        // 健康趋势点击事件
        btnTrend.setOnClickListener(v -> {
            // 跳转到健康趋势页面
        });

        // 健康报告点击事件
        btnReport.setOnClickListener(v -> {
            // 跳转到健康报告页面
        });
    }
}
