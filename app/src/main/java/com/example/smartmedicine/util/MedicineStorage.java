package com.example.smartmedicine.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartmedicine.base.MedicineInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MedicineStorage {
    private static final String PREFS_NAME = "medicine_prefs";
    private static final String KEY_MEDICINE_LIST = "medicine_list";
    private static final Gson gson = new Gson();

    public static void saveMedicineList(Context context, List<MedicineInfo> medicineList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(medicineList);
        editor.putString(KEY_MEDICINE_LIST, json);
        editor.apply();
    }

    public static List<MedicineInfo> loadMedicineList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MEDICINE_LIST, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<MedicineInfo>>() {}.getType();
        List<MedicineInfo> list = gson.fromJson(json, type);
        
        return list != null ? list : new ArrayList<>();
    }

    public static void addMedicine(Context context, MedicineInfo medicine) {
        List<MedicineInfo> list = loadMedicineList(context);
        list.add(0, medicine);
        saveMedicineList(context, list);
    }

    public static void removeMedicine(Context context, int position) {
        List<MedicineInfo> list = loadMedicineList(context);
        if (position >= 0 && position < list.size()) {
            list.remove(position);
            saveMedicineList(context, list);
        }
    }

    public static void clearAllMedicine(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
