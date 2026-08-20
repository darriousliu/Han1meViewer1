package io.github.daisukikaffuchino.han1meviewer.logic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import okio.Path.Companion.toPath

internal actual fun createSettingsDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath {
        (FileKit.filesDir / DATA_STORE_DIR / SETTINGS_DATA_STORE_FILE).absolutePath().toPath()
    }
