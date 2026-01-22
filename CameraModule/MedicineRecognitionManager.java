package com.example.smartmedicine.api;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.smartmedicine.base.BaseResponse;
import com.example.smartmedicine.base.MedicineInfo;
import com.example.smartmedicine.util.ImageUtils;
import com.example.smartmedicine.util.LogUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 药品识别管理类（封装药品识别接口调用逻辑）
 */
public class MedicineRecognitionManager {
    private static MedicineRecognitionManager instance;
    private final MedicineApi medicineApi;
    private final Context context;

    /**
     * 药品识别回调接口
     */
    public interface OnRecognitionCallback {
        void onSuccess(MedicineInfo medicineInfo);
        void onError(int errorCode, String errorMsg);
    }

    /**
     * 私有构造函数
     */
    private MedicineRecognitionManager(Context context) {
        this.context = context.getApplicationContext();
        this.medicineApi = RetrofitClient.createService(MedicineApi.class);
    }

    /**
     * 获取单例实例
     */
    public static synchronized MedicineRecognitionManager getInstance(Context context) {
        if (instance == null) {
            instance = new MedicineRecognitionManager(context);
        }
        return instance;
    }

    /**
     * 识别药品
     * @param bitmap 拍摄的药品图片
     * @param callback 识别结果回调
     */
    public void recognizeMedicine(Bitmap bitmap, final OnRecognitionCallback callback) {
        if (bitmap == null) {
            LogUtils.e("MedicineRecognitionManager", "图片Bitmap为空");
            if (callback != null) {
                callback.onError(ApiConfig.ERROR_CODE_IMAGE, "图片为空，无法识别");
            }
            return;
        }

        try {
            // 将Bitmap转换为Base64（使用ImageUtils）
            String imageBase64 = ImageUtils.bitmapToBase64(bitmap);
            if (imageBase64 == null) {
                LogUtils.e("MedicineRecognitionManager", "Base64转换失败");
                if (callback != null) {
                    callback.onError(ApiConfig.ERROR_CODE_IMAGE, "图片转换失败");
                }
                return;
            }

            // 创建识别请求
            MedicineApi.MedicineRecognizeRequest request = new MedicineApi.MedicineRecognizeRequest(imageBase64);
            Call<BaseResponse<MedicineInfo>> call = medicineApi.recognizeMedicine(request);

            // 异步调用API
            call.enqueue(new Callback<BaseResponse<MedicineInfo>>() {
                @Override
                public void onResponse(Call<BaseResponse<MedicineInfo>> call, Response<BaseResponse<MedicineInfo>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<MedicineInfo> baseResp = response.body();
                        if (baseResp.isSuccess() && baseResp.getData() != null) {
                            if (callback != null) {
                                callback.onSuccess(baseResp.getData());
                            }
                        } else {
                            LogUtils.e("MedicineRecognitionManager", "识别失败：" + baseResp.getMsg());
                            if (callback != null) {
                                callback.onError(baseResp.getCode(), baseResp.getMsg());
                            }
                        }
                    } else {
                        LogUtils.e("MedicineRecognitionManager", "API响应失败：" + response.message());
                        if (callback != null) {
                            callback.onError(ApiConfig.ERROR_CODE_API, "服务器响应失败");
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<MedicineInfo>> call, Throwable t) {
                    LogUtils.e("MedicineRecognitionManager", "网络请求失败：" + t.getMessage());
                    if (callback != null) {
                        callback.onError(ApiConfig.ERROR_CODE_NETWORK, "网络错误：" + t.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            LogUtils.e("MedicineRecognitionManager", "识别过程异常：" + e.getMessage());
            if (callback != null) {
                callback.onError(ApiConfig.ERROR_CODE_IMAGE, "图片处理异常：" + e.getMessage());
            }
        }
    }
}