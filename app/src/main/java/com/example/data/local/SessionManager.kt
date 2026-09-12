package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SessionManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val USER_KEY = "user_profile"
    private val AUTH_TOKEN_KEY = "auth_token"

    // Use StateFlow to maintain reactive architecture since SharedPreferences is synchronous
    private val _userFlow = MutableStateFlow(getUserFromPrefs())
    private val _tokenFlow = MutableStateFlow(sharedPreferences.getString(AUTH_TOKEN_KEY, null))

    fun getUserStream(): Flow<User?> = _userFlow.asStateFlow()

    fun getTokenStream(): Flow<String?> = _tokenFlow.asStateFlow()

    private fun getUserFromPrefs(): User? {
        val userJson = sharedPreferences.getString(USER_KEY, null)
        return if (userJson != null) {
            try {
                Json.decodeFromString<User>(userJson)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun saveUser(user: User, token: String = "mock_supabase_token") {
        sharedPreferences.edit()
            .putString(USER_KEY, Json.encodeToString(user))
            .putString(AUTH_TOKEN_KEY, token)
            .apply()
            
        _userFlow.value = user
        _tokenFlow.value = token
    }

    suspend fun updateUserAvatar(avatarUrl: String) {
        val current = _userFlow.value ?: return
        val currentToken = _tokenFlow.value ?: "mock_supabase_token"
        val updated = current.copy(profilePictureUrl = avatarUrl)
        saveUser(updated, currentToken)
    }

    suspend fun clearSession() {
        sharedPreferences.edit()
            .remove(USER_KEY)
            .remove(AUTH_TOKEN_KEY)
            .apply()
            
        _userFlow.value = null
        _tokenFlow.value = null
    }
}
