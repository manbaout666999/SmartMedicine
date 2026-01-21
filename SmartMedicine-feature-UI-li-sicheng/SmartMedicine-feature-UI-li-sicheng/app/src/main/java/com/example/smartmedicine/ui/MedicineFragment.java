package com.example.smartmedicine.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmedicine.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 药物列表Fragment
 */
public class MedicineFragment extends Fragment {

    private TextInputLayout searchLayout;
    private TextInputEditText etSearch;
    private ChipGroup chipGroup;
    private Chip chipAll;
    private Chip chipNeedRefill;
    private Chip chipExpired;
    private RecyclerView rvMedicine;
    private LinearLayout emptyView;
    private MaterialButton btnAddFirst;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medicine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initData();
        setupListeners();
    }

    private void initViews(View view) {
        searchLayout = view.findViewById(R.id.search_layout);
        etSearch = view.findViewById(R.id.et_search);
        chipGroup = view.findViewById(R.id.chip_group);
        chipAll = view.findViewById(R.id.chip_all);
        chipNeedRefill = view.findViewById(R.id.chip_need_refill);
        chipExpired = view.findViewById(R.id.chip_expired);
        rvMedicine = view.findViewById(R.id.rv_medicine);
        emptyView = view.findViewById(R.id.empty_view);
        btnAddFirst = view.findViewById(R.id.btn_add_first);
        fabAdd = view.findViewById(R.id.fab_add);
    }

    private void initData() {
        rvMedicine.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        // 搜索框监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 搜索药物
            }
        });

        // 分类筛选
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                // 显示全部
            } else if (checkedId == R.id.chip_need_refill) {
                // 显示需要补货
            } else if (checkedId == R.id.chip_expired) {
                // 显示已过期
            }
        });

        // 添加药物按钮
        fabAdd.setOnClickListener(v -> {
            // 跳转到添加药物页面
        });

        // 空状态添加按钮
        btnAddFirst.setOnClickListener(v -> {
            // 跳转到添加药物页面
        });
    }
}
