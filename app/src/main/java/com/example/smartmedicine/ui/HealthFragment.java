package com.example.smartmedicine.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartmedicine.R;
import com.example.smartmedicine.api.HealthApi;
import com.example.smartmedicine.api.RetrofitClient;
import com.example.smartmedicine.base.BaseResponse;
import com.example.smartmedicine.base.HealthData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private HealthApi healthApi;
    private HealthData lastHealthData;

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
        healthApi = RetrofitClient.createService(HealthApi.class, RetrofitClient.getInstance());

        // 初始显示占位数据
        tvLastUpdate.setText("最近更新：--");
        tvBloodPressure.setText("-- / -- mmHg");
        tvBpStatus.setText("未知");
        tvHeartRate.setText("-- 次/分");
        tvHrStatus.setText("未知");
        tvBloodSugar.setText("-- mmol/L");
        tvBsStatus.setText("未知");
    }

    private void setupListeners() {
        // 快捷记录点击事件
        cardQuickRecord.setOnClickListener(v -> {
            showQuickRecordDialog();
        });

        // 健康趋势点击事件
        btnTrend.setOnClickListener(v -> {
            // 简单使用最近一条数据给出提示，后续可接入折线图等可视化
            if (lastHealthData == null) {
                Toast.makeText(requireContext(), "暂无健康数据，请先记录一次。", Toast.LENGTH_SHORT).show();
                return;
            }
            String msg = "最近心率：" + lastHealthData.getHeartRate() + " 次/分\n"
                    + "最近血糖：" + lastHealthData.getBloodSugar() + " mmol/L\n"
                    + "最近睡眠：" + lastHealthData.getSleepHours() + " 小时\n"
                    + "步数：" + lastHealthData.getStepCount() + " 步";
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("今日健康概览")
                    .setMessage(msg)
                    .setPositiveButton("知道了", null)
                    .show();
        });

        // 健康报告点击事件
        btnReport.setOnClickListener(v -> {
            requestHealthReport();
        });
    }

    /**
     * 弹出对话框，快速录入一条健康数据，并同步到后端
     */
    private void showQuickRecordDialog() {
        if (getContext() == null) {
            return;
        }
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText etBpHigh = createNumberEditText("收缩压（高压，mmHg）");
        EditText etBpLow = createNumberEditText("舒张压（低压，mmHg）");
        EditText etHeartRate = createNumberEditText("心率（次/分）");
        EditText etBloodSugar = createDecimalEditText("血糖（mmol/L）");
        EditText etSleepHours = createNumberEditText("睡眠时长（小时）");
        EditText etSteps = createNumberEditText("步数");

        layout.addView(etBpHigh);
        layout.addView(etBpLow);
        layout.addView(etHeartRate);
        layout.addView(etBloodSugar);
        layout.addView(etSleepHours);
        layout.addView(etSteps);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("快速记录健康数据")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存并同步", (dialog, which) -> {
                    try {
                        HealthData data = new HealthData();
                        data.setCollectTime(System.currentTimeMillis());
                        data.setBloodPressureSystolic(parseFloat(etBpHigh.getText().toString()));
                        data.setBloodPressureDiastolic(parseFloat(etBpLow.getText().toString()));
                        data.setHeartRate(parseFloat(etHeartRate.getText().toString()));
                        data.setBloodSugar(parseFloat(etBloodSugar.getText().toString()));
                        data.setSleepHours((int) parseFloat(etSleepHours.getText().toString()));
                        data.setStepCount((int) parseFloat(etSteps.getText().toString()));

                        updateHealthUI(data);
                        syncHealthDataToServer(data);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private EditText createNumberEditText(String hint) {
        EditText et = new EditText(requireContext());
        et.setHint(hint);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        return et;
    }

    private EditText createDecimalEditText(String hint) {
        EditText et = new EditText(requireContext());
        et.setHint(hint);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return et;
    }

    private float parseFloat(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException("empty");
        }
        return Float.parseFloat(text.trim());
    }

    /**
     * 更新页面上的健康数据显示
     */
    private void updateHealthUI(HealthData data) {
        lastHealthData = data;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timeStr = sdf.format(new Date(data.getCollectTime()));
        tvLastUpdate.setText("最近更新：" + timeStr);

        String bp = (int) data.getBloodPressureSystolic() + "/" + (int) data.getBloodPressureDiastolic() + " mmHg";
        tvBloodPressure.setText(bp);
        tvBpStatus.setText("待评估");

        tvHeartRate.setText((int) data.getHeartRate() + " 次/分");
        tvHrStatus.setText("待评估");

        tvBloodSugar.setText(data.getBloodSugar() + " mmol/L");
        tvBsStatus.setText("待评估");
    }

    /**
     * 调用后端接口同步一条健康数据
     */
    private void syncHealthDataToServer(HealthData data) {
        if (healthApi == null) {
            return;
        }
        List<HealthData> list = new ArrayList<>();
        list.add(data);
        HealthApi.HealthDataSyncRequest request =
                new HealthApi.HealthDataSyncRequest("user-001", list);
        healthApi.syncHealthData(request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "健康数据已同步", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "同步失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "同步失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 调用后端生成周/月健康报告，并弹窗展示
     */
    private void requestHealthReport() {
        if (healthApi == null) {
            return;
        }
        HealthApi.HealthReportRequest request =
                new HealthApi.HealthReportRequest("user-001", HealthApi.HealthReportRequest.TYPE_WEEK);
        healthApi.getHealthReport(request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String report = response.body().getData();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("健康报告（本周）")
                            .setMessage(report != null ? report : "报告内容为空")
                            .setPositiveButton("关闭", null)
                            .show();
                } else {
                    Toast.makeText(requireContext(), "获取报告失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "获取报告失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
