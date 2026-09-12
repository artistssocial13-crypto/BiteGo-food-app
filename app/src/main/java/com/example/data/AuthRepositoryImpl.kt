package com.example.data

import com.example.data.local.SessionManager
import com.example.data.remote.AuthRequest
import com.example.data.remote.SupabaseAuthApi
import com.example.domain.AuthRepository
import com.example.domain.LoyaltyTier
import com.example.domain.User
import com.example.domain.UserRole
import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl(
    private val api: SupabaseAuthApi,
    private val sessionManager: SessionManager,
    private val apiKey: String
) : AuthRepository {

    override val currentUser = sessionManager.getUserStream()

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val response = api.signUp(apiKey, AuthRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val user = User(
                    id = authResponse.user.id,
                    name = email.substringBefore("@"),
                    email = authResponse.user.email,
                    role = UserRole.CUSTOMER,
                    loyaltyPoints = 0,
                    lifetimePoints = 0,
                    loyaltyTier = LoyaltyTier.BRONZE
                )
                sessionManager.saveUser(user, authResponse.accessToken)
                Result.success(user)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val response = api.signIn(apiKey, AuthRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Keep the current user details from local if it exists and matches ID
                val existingUser = sessionManager.getUserStream().firstOrNull()
                val user = if (existingUser != null && existingUser.id == authResponse.user.id) {
                    existingUser
                } else {
                    User(
                        id = authResponse.user.id,
                        name = email.substringBefore("@"),
                        email = authResponse.user.email,
                        role = UserRole.CUSTOMER,
                        loyaltyPoints = 0,
                        lifetimePoints = 0,
                        loyaltyTier = LoyaltyTier.BRONZE
                    )
                }
                
                sessionManager.saveUser(user, authResponse.accessToken)
                Result.success(user)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            // Need token to pass to logout. The simplest implementation:
            // Assuming sessionManager handles the token, though currently it just saves to dataStore.
            // A more robust implementation would read the token. For now, we just clear session.
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
