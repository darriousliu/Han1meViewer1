package io.github.daisukikaffuchino.han1meviewer.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository

abstract class BaseActivity : AppCompatActivity() {

    protected open fun beforeSuperOnCreate(savedInstanceState: Bundle?) = Unit

    protected open fun onActivityCreated(savedInstanceState: Bundle?) = Unit

    final override fun onCreate(savedInstanceState: Bundle?) {
        beforeSuperOnCreate(savedInstanceState)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        onActivityCreated(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.isNavigationBarContrastEnforced = false
    }

    override fun onResume() {
        super.onResume()
        setSecureMode(SettingsRepository.secureMode)
    }

    fun setSecureMode(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

}
