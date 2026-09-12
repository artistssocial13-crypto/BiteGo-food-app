package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.domain.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    private val USER_KEY = stringPreferencesKey("user_profile")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    fun getUserStream(): Flow<User?> {
        return context.dataStore.data.map { preferences ->
            val userJson = preferences[USER_KEY]
            if (userJson != null) {
                try {
                    Json.decodeFromString<User>(userJson)
                } catch (e: Exception) {
                    null
                }
            } else null
        }
    }

    fun getTokenStream(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }

    suspend fun saveUser(user: User, token: String = "mock_supabase_token") {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = Json.encodeToString(user)
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
}
