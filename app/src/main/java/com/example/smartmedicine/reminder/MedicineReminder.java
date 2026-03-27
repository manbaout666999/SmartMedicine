package com.example.smartmedicine.reminder;

/**
 * 用药提醒实体（当前简单保存在内存，仅用于调度闹钟）
 * 如后续需要持久化，可迁移到 Room 或本地数据库。
 */
public class MedicineReminder {

    public long id;
    public String medicineName;
    public String dosageText;
    public int hour;
    public int minute;

    public MedicineReminder() {
    }

    public MedicineReminder(long id, String medicineName, String dosageText, int hour, int minute) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosageText = dosageText;
        this.hour = hour;
        this.minute = minute;
    }
}

