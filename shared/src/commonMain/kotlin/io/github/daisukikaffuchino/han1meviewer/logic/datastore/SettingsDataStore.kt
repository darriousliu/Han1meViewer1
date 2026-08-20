package io.github.daisukikaffuchino.han1meviewer.logic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** 与 Android 上 preferencesDataStoreFile("settings") 生成的文件名保持一致 */
internal const val SETTINGS_DATA_STORE_FILE = "settings.preferences_pb"

/** 存放目录统一是「内部文件目录 / datastore」，Android 上即 files/datastore */
internal const val DATA_STORE_DIR = "datastore"

internal expect fun createSettingsDataStore(): DataStore<Preferences>
