package io.github.daisukikaffuchino.han1meviewer.logic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import io.github.daisukikaffuchino.utils.applicationContext

internal actual fun createSettingsDataStore(): DataStore<Preferences> {
    val context = applicationContext
    return PreferenceDataStoreFactory.create(
        // 老用户的设置还躺在这两个 SharedPreferences 里，迁移只有 Android 需要
        migrations = listOf(
            SharedPreferencesMigration(context, "${context.packageName}_preferences"),
            SharedPreferencesMigration(context, context.packageName),
        ),
        produceFile = { context.preferencesDataStoreFile("settings") },
    )
}
