package com.example.data

import com.example.data.local.SessionManager
import com.example.domain.AuthRepository
import com.example.domain.LoyaltyTier
import com.example.domain.User
import com.example.domain.UserRole
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val sessionManager: SessionManager
) : AuthRepository {

    override val currentUser = sessionManager.getUserStream()

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            // Supabase auth handles session storage internally, but we'll map to our user
            val authUser = supabaseClient.auth.currentUserOrNull()
            if (authUser != null) {
                val user = User(
                    id = authUser.id,
                    name = email.substringBefore("@"),
                    email = authUser.email ?: email,
                    role = UserRole.CUSTOMER,
                    loyaltyPoints = 0,
                    lifetimePoints = 0,
                    loyaltyTier = LoyaltyTier.BRONZE
                )
                sessionManager.saveUser(user, supabaseClient.auth.currentAccessTokenOrNull() ?: "")
                Result.success(user)
            } else {
                Result.failure(Exception("Sign up completed but user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val authUser = supabaseClient.auth.currentUserOrNull()
            if (authUser != null) {
                val existingUser = sessionManager.getUserStream().firstOrNull()
                val user = if (existingUser != null && existingUser.id == authUser.id) {
                    existingUser
                } else {
                    User(
                        id = authUser.id,
                        name = email.substringBefore("@"),
                        email = authUser.email ?: email,
                        role = UserRole.CUSTOMER,
                        loyaltyPoints = 0,
                        lifetimePoints = 0,
                        loyaltyTier = LoyaltyTier.BRONZE
                    )
                }
                sessionManager.saveUser(user, supabaseClient.auth.currentAccessTokenOrNull() ?: "")
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in completed but user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
