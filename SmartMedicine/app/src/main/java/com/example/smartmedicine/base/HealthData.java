package com.example.smartmedicine.base;

import java.io.Serializable;

/**
 * 健康数据实体（存储心率、血糖等核心健康指标）
 */
public class HealthData implements Serializable {
    private long collectTime;   // 数据采集时间戳（毫秒）
    private float heartRate;    // 心率（次/分钟）
    private float bloodSugar;   // 血糖（mmol/L）
    private int sleepHours;     // 睡眠时长（小时）
    private int stepCount;      // 当日步数（步）
    private float bloodPressureSystolic; // 收缩压（高压，mmHg）
    private float bloodPressureDiastolic; // 舒张压（低压，mmHg）

    // Getter & Setter（小驼峰命名规范）
    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(float heartRate) {
        this.heartRate = heartRate;
    }

    public float getBloodSugar() {
        return bloodSugar;
    }

    public void setBloodSugar(float bloodSugar) {
        this.bloodSugar = bloodSugar;
    }

    public int getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(int sleepHours) {
        this.sleepHours = sleepHours;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public float getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(float bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public float getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(float bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }
}