package com.example.smartmedicine;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.smartmedicine.ui.AIFragment;
import com.example.smartmedicine.ui.HealthFragment;
import com.example.smartmedicine.ui.HomeFragment;
import com.example.smartmedicine.ui.MedicineFragment;
import com.example.smartmedicine.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主活动 - 包含底部导航栏和Fragment容器
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // Fragment实例
    private HomeFragment homeFragment;
    private MedicineFragment medicineFragment;
    private HealthFragment healthFragment;
    private AIFragment aiFragment;
    private ProfileFragment profileFragment;
    
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFragments();
        setupListeners();
        
        // 默认显示首页
        if (savedInstanceState == null) {
            showFragment(homeFragment);
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        medicineFragment = new MedicineFragment();
        healthFragment = new HealthFragment();
        aiFragment = new AIFragment();
        profileFragment = new ProfileFragment();
    }

    /**
     * 供子Fragment调用：切换到底部某个Tab
     */
    public void switchToTab(int menuItemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(menuItemId);
        }
    }

    private void setupListeners() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_medicine) {
                showFragment(medicineFragment);
                return true;
            } else if (itemId == R.id.nav_health) {
                showFragment(healthFragment);
                return true;
            } else if (itemId == R.id.nav_ai) {
                showFragment(aiFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    /**
     * 显示Fragment
     */
    private void showFragment(Fragment fragment) {
        if (currentFragment == fragment) {
            return;
        }
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // 隐藏当前Fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        
        // 显示目标Fragment
        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_container, fragment);
        } else {
            transaction.show(fragment);
        }
        
        transaction.commit();
        currentFragment = fragment;
    }
}
