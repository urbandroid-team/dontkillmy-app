package com.urbandroid.dontkillmyapp.gui;

import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import com.urbandroid.dontkillmyapp.R;

public class ToolbarUtil {

    public static void apply(AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                activity.setSupportActionBar(toolbar);
            } catch (Exception e) {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
            EdgeToEdgeUtil.insetsTop(activity.findViewById(R.id.toolbar));
            EdgeToEdgeUtil.insetsBottom(activity.findViewById(R.id.fab));

            activity.getWindow().setDecorFitsSystemWindows(false);
            activity.getWindow().setStatusBarContrastEnforced(false);
            activity.getWindow().setNavigationBarContrastEnforced(false);
        }

    }

}
