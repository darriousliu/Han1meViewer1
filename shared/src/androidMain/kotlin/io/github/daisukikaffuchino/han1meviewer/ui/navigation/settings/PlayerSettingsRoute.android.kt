package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.github.daisukikaffuchino.utils.applicationContext

actual fun isGoogleCastAvailable(): Boolean =
    GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
