package com.espol.transport.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session")

class TokenStore(private val context: Context) {

    private val keyAccess = stringPreferencesKey("access_token")

    val tokenFlow = context.dataStore.data.map { prefs -> prefs[keyAccess] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[keyAccess] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(keyAccess) }
    }

    suspend fun getToken(): String? = tokenFlow.first()
}
