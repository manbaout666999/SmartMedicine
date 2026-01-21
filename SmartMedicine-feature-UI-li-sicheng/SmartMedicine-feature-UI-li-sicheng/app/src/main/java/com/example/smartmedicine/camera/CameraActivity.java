package com.example.smartmedicine.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.smartmedicine.MainActivity;
import com.example.smartmedicine.api.MedicineRecognitionManager;
import com.example.smartmedicine.base.MedicineInfo;
import com.example.smartmedicine.util.LogUtils;
import com.example.smartmedicine.util.PermissionUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * 相机拍摄活动（实现相机预览、拍摄、药品识别功能）
 */
public class CameraActivity extends AppCompatActivity {
    // Intent Extra键名常量
    public static final String EXTRA_MEDICINE_NAME = "medicine_name";
    public static final String EXTRA_MEDICINE_DOSAGE = "medicine_dosage";
    public static final String EXTRA_MEDICINE_USAGE = "medicine_usage";
    public static final String EXTRA_MEDICINE_USAGE_TIME = "medicine_usage_time";

    private PreviewView previewView;
    private ImageView btnBack;
    private ImageView btnCapture;
    private TextView tvTip;
    private ProgressBar progressBar;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private Executor cameraExecutor;

    private MedicineRecognitionManager recognitionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // 初始化视图
        initViews();
        // 初始化相机执行器
        cameraExecutor = ContextCompat.getMainExecutor(this);
        // 初始化药品识别管理器
        recognitionManager = MedicineRecognitionManager.getInstance(this);

        // 检查权限
        if (PermissionUtils.checkAllPermissions(this)) {
            startCamera();
        } else {
            PermissionUtils.requestAllPermissions(this);
        }

        // 设置点击事件
        setClickListeners();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        btnBack = findViewById(R.id.btn_back);
        btnCapture = findViewById(R.id.btn_capture);
        tvTip = findViewById(R.id.tv_tip);
        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 拍摄按钮
        btnCapture.setOnClickListener(v -> {
            if (imageCapture != null) {
                capturePhoto();
            }
        });
    }

    /**
     * 启动相机
     */
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                LogUtils.e("CameraActivity", "启动相机失败：" + e.getMessage());
                Toast.makeText(this, "启动相机失败", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * 绑定相机用例
     */
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // 配置预览
        Preview preview = new Preview.Builder()
                .build();

        // 配置图像捕获
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // 选择后置摄像头
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // 解除之前的绑定
        cameraProvider.unbindAll();

        // 绑定相机用例
        Camera camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture);

        // 设置预览视图
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    /**
     * 拍摄照片
     */
    private void capturePhoto() {
        if (imageCapture == null) {
            return;
        }

        // 显示加载指示器
        progressBar.setVisibility(View.VISIBLE);
        tvTip.setText("正在拍摄...");

        // 创建临时文件
        File photoFile = new File(getCacheDir(), "medicine_" + System.currentTimeMillis() + ".jpg");

        // 配置输出选项
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // 执行拍摄
        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // 拍摄成功
                        LogUtils.d("CameraActivity", "照片保存成功：" + photoFile.getAbsolutePath());
                        tvTip.setText("正在识别...");
                        // 识别药品
                        recognizeMedicine(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // 拍摄失败
                        LogUtils.e("CameraActivity", "拍摄失败：" + exception.getMessage());
                        progressBar.setVisibility(View.GONE);
                        tvTip.setText("拍摄失败，请重试");
                        Toast.makeText(CameraActivity.this, "拍摄失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 识别药品
     */
    private void recognizeMedicine(File photoFile) {
        if (photoFile == null || !photoFile.exists()) {
            LogUtils.e("CameraActivity", "照片文件不存在");
            progressBar.setVisibility(View.GONE);
            tvTip.setText("照片文件不存在");
            return;
        }

        // 加载图片
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        if (bitmap == null) {
            LogUtils.e("CameraActivity", "图片加载失败");
            progressBar.setVisibility(View.GONE);
            tvTip.setText("图片加载失败");
            return;
        }

        // 调用药品识别API
        recognitionManager.recognizeMedicine(bitmap, new MedicineRecognitionManager.OnRecognitionCallback() {
            @Override
            public void onSuccess(MedicineInfo medicineInfo) {
                // 识别成功
                LogUtils.d("CameraActivity", "药品识别成功：" + medicineInfo.getMedicineName());
                progressBar.setVisibility(View.GONE);
                tvTip.setText("识别成功");

                // 返回结果给主界面
                returnToMainActivity(medicineInfo);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                // 识别失败
                LogUtils.e("CameraActivity", "药品识别失败：" + errorMsg);
                progressBar.setVisibility(View.GONE);
                tvTip.setText("识别失败：" + errorMsg);
                Toast.makeText(CameraActivity.this, "识别失败：" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 返回结果给主界面
     */
    private void returnToMainActivity(MedicineInfo medicineInfo) {
        if (medicineInfo == null) {
            LogUtils.e("CameraActivity", "药品信息为空");
            finish();
            return;
        }

        // 创建返回Intent
        Intent resultIntent = new Intent();

        // 设置药品信息
        resultIntent.putExtra(EXTRA_MEDICINE_NAME, medicineInfo.getMedicineName());
        resultIntent.putExtra(EXTRA_MEDICINE_DOSAGE, medicineInfo.getDosage());
        resultIntent.putExtra(EXTRA_MEDICINE_USAGE, medicineInfo.getUsageMethod());
        resultIntent.putExtra(EXTRA_MEDICINE_USAGE_TIME, medicineInfo.getUsageTime());

        // 设置结果并返回
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allGranted = PermissionUtils.handlePermissionResult(this, requestCode, permissions, grantResults);

        if (allGranted) {
            // 权限已授予，启动相机
            startCamera();
        } else {
            // 权限被拒绝，返回主界面
            Toast.makeText(this, "权限不足，无法使用相机", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭相机执行器
        if (cameraExecutor != null) {
            // 相机执行器由系统管理，不需要手动关闭
        }
    }
}