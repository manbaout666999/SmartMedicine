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
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 首页Fragment - 显示今日用药和快捷操作
 */
public class HomeFragment extends Fragment {

    private TextView tvDate;
    private TextView tvMedicineCount;
    private RecyclerView rvTodayMedicine;
    private TextView tvNoMedicineToday;
    private MaterialCardView cardAddMedicine;
    private MaterialCardView cardScan;
    private RecyclerView rvUpcoming;
    private TextView tvViewAll;

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

        // 设置今日用药数量（示例数据）
        tvMedicineCount.setText("今日需服用 3 种药物");

        // 设置RecyclerView
        rvTodayMedicine.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        // 添加药物点击事件
        cardAddMedicine.setOnClickListener(v -> {
            // 跳转到添加药物页面
            if (getActivity() != null) {
                // startActivity(new Intent(getActivity(), AddMedicineActivity.class));
            }
        });

        // 扫码识别点击事件
        cardScan.setOnClickListener(v -> {
            // 扫码识别功能
        });

        // 查看全部点击事件
        tvViewAll.setOnClickListener(v -> {
            // 跳转到药物列表
        });
    }
}
