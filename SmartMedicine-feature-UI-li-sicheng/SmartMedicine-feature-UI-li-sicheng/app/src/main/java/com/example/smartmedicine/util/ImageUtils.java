package com.example.smartmedicine.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import com.example.smartmedicine.api.ApiConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片处理工具类（压缩、Base64转换、旋转、保存）
 */
public class ImageUtils {
    /**
     * 图片压缩（按尺寸+质量双重压缩，适配API上传）
     * @param imagePath 原图路径
     * @return 压缩后的Bitmap
     */
    public static Bitmap compressImage(String imagePath) {
        if (imagePath == null || !new File(imagePath).exists()) {
            LogUtils.e("ImageUtils", "图片路径无效或文件不存在");
            return null;
        }

        // 1. 按尺寸压缩（避免OOM）
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 仅获取图片尺寸，不加载 bitmap
        BitmapFactory.decodeFile(imagePath, options);

        int targetWidth = 1280; // 目标宽度（适配移动端API）
        int targetHeight = 720; // 目标高度
        int inSampleSize = 1;   // 缩放比例

        // 计算缩放比例（宽高均不超过目标尺寸）
        if (options.outWidth > targetWidth || options.outHeight > targetHeight) {
            int widthRatio = Math.round((float) options.outWidth / targetWidth);
            int heightRatio = Math.round((float) options.outHeight / targetHeight);
            inSampleSize = Math.min(widthRatio, heightRatio); // 取最小缩放比例
        }

        // 加载压缩后的 bitmap
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // 降低像素格式（减少内存占用）
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        // 2. 按质量压缩（控制文件大小≤500KB）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 80; // 初始压缩质量
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        while (baos.toByteArray().length > 500 * 1024) { // 500KB
            baos.reset();
            quality -= 10; // 每次降低10%质量
            if (quality < 10) break; // 最低质量10%
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        LogUtils.d("ImageUtils", "图片压缩完成，大小：" + (baos.toByteArray().length / 1024) + "KB");
        return bitmap;
    }

    /**
     * Bitmap转Base64字符串（用于API上传）
     * @param bitmap 压缩后的Bitmap
     * @return Base64字符串（不含前缀）
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            LogUtils.e("ImageUtils", "Bitmap为空，Base64转换失败");
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();

        // Base64编码（NO_WRAP：不添加换行符）
        String base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        LogUtils.d("ImageUtils", "Base64转换完成，长度：" + base64Str.length());
        return base64Str;
    }

    /**
     * 图片旋转（解决相机拍摄图片旋转问题）
     * @param bitmap 原图Bitmap
     * @param degrees 旋转角度（如90°、180°）
     * @return 旋转后的Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || bitmap == null) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees); // 旋转指定角度
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
        );
        bitmap.recycle(); // 回收原Bitmap，避免内存泄漏
        return rotatedBitmap;
    }

    /**
     * 保存Bitmap到本地缓存（可选，用于调试或离线存储）
     * @param bitmap 图片Bitmap
     * @param cacheDir 缓存目录
     * @return 保存后的文件路径
     */
    public static String saveBitmapToCache(Bitmap bitmap, File cacheDir) {
        if (bitmap == null || cacheDir == null) {
            LogUtils.e("ImageUtils", "Bitmap或缓存目录为空，保存失败");
            return null;
        }

        // 创建缓存文件（以时间戳命名，避免重复）
        File imageFile = new File(cacheDir, "medicine_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            LogUtils.d("ImageUtils", "图片保存成功：" + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            LogUtils.e("ImageUtils", "图片保存失败：" + e.getMessage());
            return null;
        }
    }
}