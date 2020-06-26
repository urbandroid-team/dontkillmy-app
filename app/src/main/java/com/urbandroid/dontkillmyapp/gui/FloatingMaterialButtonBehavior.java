package com.urbandroid.dontkillmyapp.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class FloatingMaterialButtonBehavior extends CoordinatorLayout.Behavior<MaterialButton> {
    private float mFabTranslationY;

    public FloatingMaterialButtonBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final MaterialButton child, final View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final MaterialButton child, final View dependency) {
//        Logger.logInfo("Behavior: onDependentViewChanged " + dependency);
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child, dependency);
            ViewCompat.setTranslationY(child, mFabTranslationY);
        }
        return false;
    }

    @Override
    public void onDependentViewRemoved(final CoordinatorLayout parent, final MaterialButton child, final View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);
//        Logger.logInfo("Behavior: onDependentViewRemoved " + dependency);
        ViewCompat.setTranslationY(child, 0);
    }

    private void updateFabTranslationForSnackbar(CoordinatorLayout parent, final MaterialButton fab, View snackbar) {
        mFabTranslationY = getFabTranslationYForSnackbar(parent, fab, snackbar);
//        Logger.logInfo("Behavior: updateFabTranslationForSnackbar " + mFabTranslationY);
    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent, MaterialButton fab, View snackbar) {
        float minOffset = 0;
        minOffset = Math.min(minOffset, ViewCompat.getTranslationY(snackbar) - snackbar.getHeight());
//        Logger.logInfo("Behavior: onDependentViewRemoved " + minOffset);
        return minOffset;
    }
}