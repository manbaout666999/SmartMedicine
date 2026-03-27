package com.example.smartmedicine.base;

import java.io.Serializable;

/**
 * 药品信息实体（存储药品核心属性，支持序列化）
 */
public class MedicineInfo implements Serializable {
    private String medicineName;    // 药品名称
    private String dosage;          // 服用剂量（如"每日2次，每次1片"）
    private String usageTime;       // 服用时间（如"早8点、晚8点"）
    private String usageMethod;     // 服用方式（如"饭后温水送服"）
    private String expirationDate;  // 有效期（如"2026-12-31"）
    private String allergens;       // 过敏原（如"青霉素、海鲜"）
    private String warning;         // 注意事项（如"孕妇禁用、避免饮酒"）
    private String manufacturer;    // 生产厂家（可选）
    private String imagePath;       // 药品图片路径

    // Getter & Setter（小驼峰命名规范）
    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(String usageTime) {
        this.usageTime = usageTime;
    }

    public String getUsageMethod() {
        return usageMethod;
    }

    public void setUsageMethod(String usageMethod) {
        this.usageMethod = usageMethod;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}