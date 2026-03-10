package com.example.smartmedicine.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartmedicine.R;
import com.example.smartmedicine.MainActivity;
import com.google.android.material.card.MaterialCardView;
import android.widget.CompoundButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * 个人中心Fragment
 */
public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvVersion;
    private MaterialCardView cardMyMedicine;
    private MaterialCardView cardHealthData;
    private MaterialCardView cardReminder;
    private MaterialCardView cardSettings;
    private MaterialCardView cardAbout;
    private MaterialCardView cardFeedback;
    private SwitchMaterial switchReminder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initData();
        setupListeners();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvName = view.findViewById(R.id.tv_name);
        tvVersion = view.findViewById(R.id.tv_version);
        cardMyMedicine = view.findViewById(R.id.card_my_medicine);
        cardHealthData = view.findViewById(R.id.card_health_data);
        cardReminder = view.findViewById(R.id.card_reminder);
        cardSettings = view.findViewById(R.id.card_settings);
        cardAbout = view.findViewById(R.id.card_about);
        cardFeedback = view.findViewById(R.id.card_feedback);
        switchReminder = view.findViewById(R.id.switch_reminder);
    }

    private void initData() {
        // 设置版本号
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvVersion.setText("版本 " + versionName);
        } catch (Exception e) {
            tvVersion.setText("版本 1.0.0");
        }

        // 加载用药提醒开关状态
        boolean enableReminder = getReminderEnabled(requireContext());
        switchReminder.setChecked(enableReminder);
    }

    private void setupListeners() {
        // 我的药物
        cardMyMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToTab(R.id.nav_medicine);
                }
            }
        });

        // 健康数据
        cardHealthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToTab(R.id.nav_health);
                }
            }
        });

        // 用药提醒开关
        switchReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveReminderEnabled(requireContext(), isChecked);
            }
        });

        // 设置
        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到设置页面
            }
        });

        // 关于
        cardAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到关于页面
            }
        });

        // 意见反馈
        cardFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到意见反馈页面
            }
        });
    }

    private static final String PREF_NAME = "smart_medicine_pref";
    private static final String KEY_REMINDER_ENABLE = "key_reminder_enable";

    private boolean getReminderEnabled(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_REMINDER_ENABLE, true);
    }

    private void saveReminderEnabled(Context context, boolean enable) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_REMINDER_ENABLE, enable).apply();
    }
}
