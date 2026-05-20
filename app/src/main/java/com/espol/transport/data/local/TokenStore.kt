package com.espol.transport.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session")

class TokenStore(private val context: Context) {

    private val keyAccess = stringPreferencesKey("access_token")
    private val keyStayLoggedIn = booleanPreferencesKey("stay_logged_in")

    val tokenFlow = context.dataStore.data.map { prefs -> prefs[keyAccess] }

    suspend fun saveToken(token: String, stayLoggedIn: Boolean) {
        context.dataStore.edit {
            it[keyAccess] = token
            it[keyStayLoggedIn] = stayLoggedIn
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(keyAccess)
            it.remove(keyStayLoggedIn)
        }
    }

    suspend fun getToken(): String? = tokenFlow.first()

    suspend fun shouldAutoLogin(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[keyStayLoggedIn] == true && !prefs[keyAccess].isNullOrBlank()
    }
}
