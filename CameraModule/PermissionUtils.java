package com.example.smartmedicine.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 权限工具类（封装相机、存储权限的检查与申请）
 */
public class PermissionUtils {
    // 权限请求码
    public static final int REQUEST_CODE_CAMERA = 1001;
    public static final int REQUEST_CODE_STORAGE = 1002;
    public static final int REQUEST_CODE_ALL = 1003;

    // 权限常量
    public static final String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    public static final String[] PERMISSIONS_ALL = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    /**
     * 检查相机权限是否已授予
     */
    public static boolean checkCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查存储权限是否已授予
     */
    public static boolean checkStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * 检查所有需要的权限是否已授予
     */
    public static boolean checkAllPermissions(Context context) {
        return checkCameraPermission(context) && checkStoragePermission(context);
    }

    /**
     * 申请相机权限
     */
    public static void requestCameraPermission(Activity activity) {
        if (!checkCameraPermission(activity)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_CAMERA, REQUEST_CODE_CAMERA);
        }
    }

    /**
     * 申请存储权限
     */
    public static void requestStoragePermission(Activity activity) {
        if (!checkStoragePermission(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_CODE_STORAGE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE);
            }
        }
    }

    /**
     * 申请所有需要的权限
     */
    public static void requestAllPermissions(Activity activity) {
        if (!checkAllPermissions(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_ALL, REQUEST_CODE_ALL);
            } else {
                ActivityCompat.requestPermissions(activity, 
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ALL);
            }
        }
    }

    /**
     * 处理权限申请结果
     */
    public static boolean handlePermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        boolean allGranted = true;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            showPermissionDeniedToast(activity, requestCode);
        }

        return allGranted;
    }

    /**
     * 显示权限被拒绝的提示
     */
    private static void showPermissionDeniedToast(Activity activity, int requestCode) {
        String message = "";

        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                message = "相机权限被拒绝，无法进行拍摄";
                break;
            case REQUEST_CODE_STORAGE:
                message = "存储权限被拒绝，无法保存图片";
                break;
            case REQUEST_CODE_ALL:
                message = "必要权限被拒绝，无法使用拍摄功能";
                break;
            default:
                message = "权限被拒绝";
                break;
        }

        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查是否应该显示权限请求解释
     */
    public static boolean shouldShowPermissionRationale(Activity activity, int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA);
            case REQUEST_CODE_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            default:
                return false;
        }
    }
}