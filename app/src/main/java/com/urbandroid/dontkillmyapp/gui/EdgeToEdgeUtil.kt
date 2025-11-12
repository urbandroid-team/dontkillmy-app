package com.urbandroid.dontkillmyapp.gui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

class EdgeToEdgeUtil {

    companion object {

        @JvmOverloads
        @JvmStatic
        fun insetsBottom(v: View?, additionalMargin : Int = 0, consume :Boolean = true) {
            v?.apply {
                ViewCompat.setOnApplyWindowInsetsListener(v) { it, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    // Apply the insets as a margin to the view. This solution sets
                    // only the bottom, left, and right dimensions, but you can apply whichever
                    // insets are appropriate to your layout. You can also update the view padding
                    // if that's more appropriate.
                    it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        if (insets.left > 0) {
                            leftMargin = insets.left
                        }
                        if (insets.right > 0) {
                            rightMargin = insets.right
                        }
                        bottomMargin = (insets.bottom + additionalMargin)
                    }

                    // Return CONSUMED if you don't want want the window insets to keep passing
                    // down to descendant views.
                    if (consume) {
                        WindowInsetsCompat.CONSUMED
                    }
                    WindowInsetsCompat.Builder().build()
                }
            }

        }

        @JvmOverloads
        @JvmStatic
        fun insetsHorizontal(v: View?, additionalMargin : Int = 0, consume :Boolean = true) {
            v?.apply {
                ViewCompat.setOnApplyWindowInsetsListener(v) { it, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    // Apply the insets as a margin to the view. This solution sets
                    // only the bottom, left, and right dimensions, but you can apply whichever
                    // insets are appropriate to your layout. You can also update the view padding
                    // if that's more appropriate.
                    it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        if (insets.left > 0) {
                            leftMargin = insets.left
                        }
                        if (insets.right > 0) {
                            rightMargin = insets.right
                        }
                    }

                    // Return CONSUMED if you don't want want the window insets to keep passing
                    // down to descendant views.
                    if (consume) {
                        WindowInsetsCompat.CONSUMED
                    }
                    WindowInsetsCompat.Builder().build()
                }
            }

        }

        @JvmOverloads
        @JvmStatic
        fun insetsHeight(v: View?, additionalMargin : Int = 0) {
            v?.apply {
                ViewCompat.setOnApplyWindowInsetsListener(v) { it, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    // Apply the insets as a margin to the view. This solution sets
                    // only the bottom, left, and right dimensions, but you can apply whichever
                    // insets are appropriate to your layout. You can also update the view padding
                    // if that's more appropriate.
                    it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        if (insets.left > 0) {
                            leftMargin = insets.left
                        }
                        if (insets.right > 0) {
                            rightMargin = insets.right
                        }
                        height = (insets.top + additionalMargin)
                    }

                    // Return CONSUMED if you don't want want the window insets to keep passing
                    // down to descendant views.
                    WindowInsetsCompat.CONSUMED
                }
            }

        }

        @JvmOverloads
        @JvmStatic
        fun insetsTop(v: View?, additionalMargin : Int = 0) {
            v?.apply {
                ViewCompat.setOnApplyWindowInsetsListener(v) { it, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    // Apply the insets as a margin to the view. This solution sets
                    // only the bottom, left, and right dimensions, but you can apply whichever
                    // insets are appropriate to your layout. You can also update the view padding
                    // if that's more appropriate.
                    it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        if (insets.left > 0) {
                            leftMargin = insets.left
                        }
                        if (insets.right > 0) {
                            rightMargin = insets.right
                        }
                        topMargin = (insets.top + additionalMargin)
                    }

                    // Return CONSUMED if you don't want want the window insets to keep passing
                    // down to descendant views.
                    WindowInsetsCompat.CONSUMED
                }
            }

        }

        @JvmOverloads
        @JvmStatic
        fun insetsVertical(v: View?, additionalMargin : Int = 0) {
            v?.apply {
                ViewCompat.setOnApplyWindowInsetsListener(v) { it, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    // Apply the insets as a margin to the view. This solution sets
                    // only the bottom, left, and right dimensions, but you can apply whichever
                    // insets are appropriate to your layout. You can also update the view padding
                    // if that's more appropriate.
                    it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        if (insets.left > 0) {
                            leftMargin = insets.left
                        }
                        if (insets.right > 0) {
                            rightMargin = insets.right
                        }
                        topMargin = (insets.top + additionalMargin)
                        bottomMargin = (insets.top + additionalMargin)
                    }

                    // Return CONSUMED if you don't want want the window insets to keep passing
                    // down to descendant views.
                    WindowInsetsCompat.CONSUMED
                }
            }

        }


    }
}