package com.example.app_ecosense.accesibilitat

import android.os.Build
import android.view.Window
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ocultarBarra (private val window: Window) {

    fun hideSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

    }

    /*   fun showSystemNavigationBar() {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
               val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
               windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
           }
       }

     */
}