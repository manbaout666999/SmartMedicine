package com.example.smartmedicine.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmedicine.R;
import com.example.smartmedicine.api.OpenAIChatManager;
import com.example.smartmedicine.base.ChatMessage;
import com.example.smartmedicine.ui.ChatAdapter;

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
    private ChatAdapter chatAdapter;

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

        // 初始化 RecyclerView
        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter();
        rvChat.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        // 快捷问题点击：将问题文本填入输入框
        chipQ1.setOnClickListener(v -> etInput.setText(chipQ1.getText().toString()));
        chipQ2.setOnClickListener(v -> etInput.setText(chipQ2.getText().toString()));
        chipQ3.setOnClickListener(v -> etInput.setText(chipQ3.getText().toString()));
        chipQ4.setOnClickListener(v -> etInput.setText(chipQ4.getText().toString()));

        // 发送按钮点击事件
        fabSend.setOnClickListener(v -> {
            String message = etInput.getText() != null ? etInput.getText().toString().trim() : "";
            if (!message.isEmpty()) {
                // 1. 添加用户消息到聊天列表
                chatAdapter.addMessage(new ChatMessage(message, ChatMessage.TYPE_USER));

                // 2. 滚动到底部
                rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

                // 3. 清空输入框
                etInput.setText("");

                // 4. 调用 OpenAI 兼容的 AI 服务（suanli.cn）
                OpenAIChatManager.sendChatMessage(message, new OpenAIChatManager.OnChatCallback() {
                    @Override
                    public void onSuccess(String reply) {
                        requireActivity().runOnUiThread(() -> {
                            chatAdapter.addMessage(new ChatMessage(reply, ChatMessage.TYPE_AI));
                            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        });
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        requireActivity().runOnUiThread(() -> {
                            String errorText = "抱歉，我暂时无法回答。\n错误：" + errorMsg;
                            chatAdapter.addMessage(new ChatMessage(errorText, ChatMessage.TYPE_AI));
                            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        });
                    }
                });
            }
        });
    }
}