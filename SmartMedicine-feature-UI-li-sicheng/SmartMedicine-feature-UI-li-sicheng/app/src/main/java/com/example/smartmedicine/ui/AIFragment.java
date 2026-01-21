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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * AI助手Fragment
 */
public class AIFragment extends Fragment {

    private RecyclerView rvChat;
    private ChipGroup chipGroupQuick;
    private Chip chipQ1;
    private Chip chipQ2;
    private Chip chipQ3;
    private Chip chipQ4;
    private TextInputLayout tilInput;
    private TextInputEditText etInput;
    private FloatingActionButton fabSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        rvChat = view.findViewById(R.id.rv_chat);
        chipGroupQuick = view.findViewById(R.id.chip_group_quick);
        chipQ1 = view.findViewById(R.id.chip_q1);
        chipQ2 = view.findViewById(R.id.chip_q2);
        chipQ3 = view.findViewById(R.id.chip_q3);
        chipQ4 = view.findViewById(R.id.chip_q4);
        tilInput = view.findViewById(R.id.til_input);
        etInput = view.findViewById(R.id.et_input);
        fabSend = view.findViewById(R.id.fab_send);

        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        // 快捷问题点击事件
        chipQ1.setOnClickListener(v -> {
            etInput.setText(chipQ1.getText());
        });

        chipQ2.setOnClickListener(v -> {
            etInput.setText(chipQ2.getText());
        });

        chipQ3.setOnClickListener(v -> {
            etInput.setText(chipQ3.getText());
        });

        chipQ4.setOnClickListener(v -> {
            etInput.setText(chipQ4.getText());
        });

        // 发送按钮点击事件
        fabSend.setOnClickListener(v -> {
            String message = etInput.getText() != null ? etInput.getText().toString().trim() : "";
            if (!message.isEmpty()) {
                // 发送消息给AI
                etInput.setText("");
            }
        });
    }
}
