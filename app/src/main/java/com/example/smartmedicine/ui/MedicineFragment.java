package com.example.smartmedicine.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmedicine.R;
import com.example.smartmedicine.api.MedicineApi;
import com.example.smartmedicine.api.QwenVLManager;
import com.example.smartmedicine.api.RetrofitClient;
import com.example.smartmedicine.base.MedicineInfo;
import com.example.smartmedicine.reminder.MedicineReminder;
import com.example.smartmedicine.reminder.ReminderScheduler;
import com.example.smartmedicine.util.MedicineStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private MedicineApi medicineApi;
    private MedicineListAdapter medicineListAdapter;
    private final List<MedicineInfo> allMedicineList = new ArrayList<>();
    private File currentPhotoFile;
    private Uri currentPhotoUri;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openSystemCamera();
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "未授予相机权限，无法拍摄药盒照片", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result && currentPhotoFile != null) {
                    uploadAndRecognize(currentPhotoFile);
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "未获取到有效照片", Toast.LENGTH_SHORT).show();
                }
            });

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
        medicineApi = RetrofitClient.createService(MedicineApi.class, RetrofitClient.getInstance());
        
        // 加载本地保存的药品数据
        if (getContext() != null) {
            allMedicineList.clear();
            allMedicineList.addAll(MedicineStorage.loadMedicineList(getContext()));
        }
        
        medicineListAdapter = new MedicineListAdapter(allMedicineList);
        medicineListAdapter.setOnMedicineActionListener(new MedicineListAdapter.OnMedicineActionListener() {
            @Override
            public void onEditMedicine(int position) {
                showEditMedicineDialog(position);
            }

            @Override
            public void onDeleteMedicine(int position) {
                showDeleteConfirmDialog(position);
            }
        });
        rvMedicine.setAdapter(medicineListAdapter);
        updateEmptyView();
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterMedicineList(s.toString());
            }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                filterMedicineList(etSearch.getText() != null ? etSearch.getText().toString() : "");
            } else if (checkedId == R.id.chip_need_refill) {
                filterByStatus("need_refill");
            } else if (checkedId == R.id.chip_expired) {
                filterByStatus("expired");
            }
        });

        fabAdd.setOnClickListener(v -> {
            startTakeMedicinePhotoFlow();
        });

        btnAddFirst.setOnClickListener(v -> {
            startTakeMedicinePhotoFlow();
        });
    }

    private void startTakeMedicinePhotoFlow() {
        if (getContext() == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openSystemCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openSystemCamera() {
        if (getContext() == null) {
            return;
        }
        try {
            currentPhotoFile = createImageFile();
        } catch (IOException e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }
            currentPhotoFile = null;
            return;
        }

        if (currentPhotoFile == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String authority = requireContext().getPackageName() + ".fileprovider";
        currentPhotoUri = FileProvider.getUriForFile(requireContext(), authority, currentPhotoFile);
        takePictureLauncher.launch(currentPhotoUri);
    }

    private File createImageFile() throws IOException {
        if (getContext() == null) {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "MEDICINE_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = requireContext().getCacheDir();
        }
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void uploadAndRecognize(File imageFile) {
        if (getContext() == null) {
            return;
        }

        useAIModelForRecognition(imageFile);
    }

    private void useAIModelForRecognition(File imageFile) {
        if (!isAdded()) {
            return;
        }
        
        Toast.makeText(requireContext(), "正在使用AI识别药品并获取详细信息...", Toast.LENGTH_LONG).show();
        
        final String imagePath = imageFile.getAbsolutePath();
        
        QwenVLManager.recognizeAndGetDetails(imageFile, new QwenVLManager.OnMedicineDetailCallback() {
            @Override
            public void onSuccess(String medicineName, String dosage, String details, String suggestedTime) {
                if (!isAdded()) {
                    return;
                }
                
                MedicineInfo info = new MedicineInfo();
                info.setMedicineName(medicineName);
                info.setDosage(dosage);
                info.setWarning(details);
                info.setImagePath(imagePath);
                handleRecognizeSuccess(info, suggestedTime);
                
                Toast.makeText(requireContext(), 
                        "成功识别: " + medicineName, 
                        Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String errorMsg) {
                if (!isAdded()) {
                    return;
                }
                
                Toast.makeText(requireContext(), 
                        "AI识别失败: " + errorMsg + "\n请手动输入", 
                        Toast.LENGTH_LONG).show();
                
                showManualInputDialog();
            }
        });
    }

    private void handleRecognizeSuccess(MedicineInfo info, String suggestedTime) {
        if (getContext() == null) {
            return;
        }
        String medicineName = info.getMedicineName();
        String dosageText = info.getDosage();

        Calendar calendar = Calendar.getInstance();
        
        // 尝试解析AI建议的时间
        if (suggestedTime != null && !suggestedTime.isEmpty()) {
            try {
                String[] timeParts = suggestedTime.split(":");
                if (timeParts.length >= 2) {
                    int hour = Integer.parseInt(timeParts[0].trim());
                    int minute = Integer.parseInt(timeParts[1].trim());
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    
                    // 如果时间已经过了，设置到明天
                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } else {
                    calendar.add(Calendar.MINUTE, 1);
                }
            } catch (Exception e) {
                calendar.add(Calendar.MINUTE, 1);
            }
        } else {
            calendar.add(Calendar.MINUTE, 1);
        }

        if (TextUtils.isEmpty(medicineName)) {
            medicineName = "药品";
        }
        if (TextUtils.isEmpty(dosageText)) {
            dosageText = "请按说明服用";
        }

        info.setMedicineName(medicineName);
        info.setDosage(dosageText);
        info.setUsageTime(calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MINUTE)));
        allMedicineList.add(0, info);
        medicineListAdapter.notifyDataSetChanged();
        updateEmptyView();

        // 保存到本地存储
        MedicineStorage.saveMedicineList(getContext(), allMedicineList);

        MedicineReminder reminder = new MedicineReminder(
                System.currentTimeMillis(),
                info.getMedicineName(),
                info.getDosage(),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );
        ReminderScheduler.scheduleReminder(requireContext(), reminder);

        Toast.makeText(requireContext(),
                "已识别 " + info.getMedicineName() + "，并创建用药提醒（约 1 分钟后）",
                Toast.LENGTH_LONG).show();
    }

    private void showManualInputDialog() {
        if (!isAdded()) {
            return;
        }
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextInputLayout tilName = new TextInputLayout(requireContext());
        TextInputEditText etName = new TextInputEditText(requireContext());
        tilName.setHint("药品名称");
        tilName.addView(etName);

        TextInputLayout tilDosage = new TextInputLayout(requireContext());
        TextInputEditText etDosage = new TextInputEditText(requireContext());
        tilDosage.setHint("服用剂量，如：每日2次，每次1片");
        tilDosage.addView(etDosage);

        layout.addView(tilName);
        layout.addView(tilDosage);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("添加药品")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(), "请填写药品名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(dosage)) {
                        dosage = "请按说明服用";
                    }
                    
                    MedicineInfo info = new MedicineInfo();
                    info.setMedicineName(name);
                    info.setDosage(dosage);
                    handleRecognizeSuccess(info, null);
                })
                .show();
    }

    private void updateEmptyView() {
        if (allMedicineList.isEmpty()) {
            rvMedicine.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvMedicine.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void filterMedicineList(String keyword) {
        if (keyword == null) keyword = "";
        keyword = keyword.trim();
        medicineListAdapter.filter(keyword);
    }

    private void filterByStatus(String status) {
        filterMedicineList(etSearch.getText() != null ? etSearch.getText().toString() : "");
    }

    private void showEditMedicineDialog(int position) {
        if (!isAdded() || getContext() == null) {
            return;
        }
        
        MedicineInfo info = allMedicineList.get(position);
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextInputLayout tilName = new TextInputLayout(requireContext());
        TextInputEditText etName = new TextInputEditText(requireContext());
        etName.setText(info.getMedicineName());
        tilName.setHint("药品名称");
        tilName.addView(etName);

        TextInputLayout tilDosage = new TextInputLayout(requireContext());
        TextInputEditText etDosage = new TextInputEditText(requireContext());
        etDosage.setText(info.getDosage());
        tilDosage.setHint("服用剂量，如：每日2次，每次1片");
        tilDosage.addView(etDosage);

        TextInputLayout tilTime = new TextInputLayout(requireContext());
        TextInputEditText etTime = new TextInputEditText(requireContext());
        etTime.setText(info.getUsageTime());
        tilTime.setHint("服用时间，如：08:00");
        tilTime.addView(etTime);

        layout.addView(tilName);
        layout.addView(tilDosage);
        layout.addView(tilTime);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("编辑药品")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";
                    String time = etTime.getText() != null ? etTime.getText().toString().trim() : "";
                    
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(), "请填写药品名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(dosage)) {
                        dosage = "请按说明服用";
                    }
                    
                    info.setMedicineName(name);
                    info.setDosage(dosage);
                    info.setUsageTime(time);
                    
                    medicineListAdapter.notifyDataSetChanged();
                    updateEmptyView();
                    
                    if (getContext() != null) {
                        MedicineStorage.saveMedicineList(getContext(), allMedicineList);
                    }
                    
                    Toast.makeText(requireContext(), "药品信息已更新", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showDeleteConfirmDialog(int position) {
        if (!isAdded() || getContext() == null) {
            return;
        }
        
        MedicineInfo info = allMedicineList.get(position);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除药品")
                .setMessage("确定要删除 \"" + info.getMedicineName() + "\" 吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    allMedicineList.remove(position);
                    medicineListAdapter.notifyDataSetChanged();
                    updateEmptyView();
                    
                    if (getContext() != null) {
                        MedicineStorage.saveMedicineList(getContext(), allMedicineList);
                    }
                    
                    Toast.makeText(requireContext(), "药品已删除", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
