package io.github.daisukikaffuchino.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import io.github.daisukikaffuchino.utils.LogUtil
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.ANIME_URL
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_URL
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.logout
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.VideoPageHost
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.AccountRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HanimeScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.LoginRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.TopLevelBackStack
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.VideoRoute
import io.github.daisukikaffuchino.han1meviewer.ui.screen.main.MainActivityContent
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.HomePageViewModel
import io.github.daisukikaffuchino.utils.ActivityManager
import io.github.daisukikaffuchino.utils.isX86_64Device
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.LocalMainBackStack
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.handleMainIntent

class MainActivity : BaseActivity() {

    val viewModel by viewModels<HomePageViewModel>()

    val mainBackStack: TopLevelBackStack<HanimeScreen>
        get() = viewModel.mainBackStack
    private var currentVideoHost: VideoPageHost? = null
    private val pendingNavigationRequests = MutableSharedFlow<Intent>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    companion object {
        const val ACTION_TOGGLE_PLAY = "io.github.daisukikaffuchino.han1meviewer.ACTION_TOGGLE_PLAY"
    }

    private var hasAuthenticated = false
    private val pipActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LogUtil.i("pipmode", "✅ onReceive called with action: ${intent?.action}")
            when (intent?.action) {
                ACTION_TOGGLE_PLAY -> {
                    LogUtil.i("pipmode", "🎬 ACTION_TOGGLE_PLAY triggered")
                    togglePlayPause()
                }
            }
        }
    }

    private fun initData() {
        setHanimeContent {
            CompositionLocalProvider(LocalMainBackStack provides mainBackStack) {
                MainActivityContent(
                    activity = this,
                    viewModel = viewModel,
                    onOpenClipboardVideo = ::showVideoDetailFragment,
                )
            }
        }
        lifecycleScope.launch {
            pendingNavigationRequests.collect { mainBackStack.handleMainIntent(it) }
        }
    }

    override fun beforeSuperOnCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen().apply {
                setKeepOnScreenCondition { !hasAuthenticated }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val useLock = SettingsRepository.current.useLockScreen

        if (useLock && isDeviceSecureCompat(this)) {
            authenticate(
                this,
                onSuccess = {
                    hasAuthenticated = true
                    viewModel.onAuthenticated()
                    initData()
                },
                onFailed = {
                    finish()
                }
            )
        } else {
            hasAuthenticated = true
            viewModel.onAuthenticated()
            initData()
        }
        pendingNavigationRequests.tryEmit(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavigationRequests.tryEmit(intent)
    }

    private fun isDeviceSecureCompat(context: Context): Boolean {
        val km = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return km.isDeviceSecure
    }

    private fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    // 指纹被识别但不匹配（单次）
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // 取消、锁定、连续失败后触发
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_request))
            .setSubtitle(getString(R.string.unlock_method))
            .setDescription(getString(R.string.unlock_desc))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onStart() {
        super.onStart()
        registerPipReceiver()
    }

    private fun registerPipReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_TOGGLE_PLAY)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipActionReceiver, filter, RECEIVER_NOT_EXPORTED)
            LogUtil.i("pipmode", "✅ registerReceiver with RECEIVER_NOT_EXPORTED")
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            registerReceiver(pipActionReceiver, filter)
            LogUtil.i("pipmode", "✅ registerReceiver (legacy)")
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(pipActionReceiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (mainBackStack.removeLast()) {
            true
        } else {
            super.onSupportNavigateUp()
        }
    }

    fun openLogin() = viewModel.openLogin()

    fun showLogoutConfirmDialog(closeCurrentPageOnConfirm: Boolean = false) =
        viewModel.showLogoutConfirmDialog(closeCurrentPageOnConfirm)

    fun logoutWithRefresh() = viewModel.logoutWithRefresh()

    fun showVideoDetailFragment(videoCode: String, fileUri: String? = null) =
        viewModel.openVideo(videoCode, fileUri)

    fun registerCurrentVideoHost(host: VideoPageHost?) {
        currentVideoHost = host
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val currentFragment = currentVideoHost

        val allowPip = SettingsRepository.current.allowPipMode

        LogUtil.i("pipmode", "enter pip mode?\n$currentFragment\nallowpip:$allowPip\n")

        if (currentFragment?.shouldEnterPip() == true && allowPip) {
            LogUtil.i("pipmode", "enter pip mode")
            currentFragment.enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        val currentFragment = currentVideoHost

        currentFragment?.onPipModeChanged(isInPictureInPictureMode)
    }

    fun togglePlayPause() {
        currentVideoHost?.togglePlayPause()
    }

    init {
        if (!(BuildConfig.DEBUG && isX86_64Device)) {
            System.loadLibrary("chino")
        }
    }
}
