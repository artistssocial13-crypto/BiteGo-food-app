package com.example.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID

class SupabaseStorageService(
    private val supabaseClient: SupabaseClient
) {
    companion object {
        const val AVATARS_BUCKET = "avatars"
        const val RESTAURANT_BANNERS_BUCKET = "restaurant-banners"
    }

    suspend fun uploadUserAvatar(userId: String, imageBytes: ByteArray, extension: String = "jpg"): Result<String> {
        return runCatching {
            val fileName = "$userId/avatar_${System.currentTimeMillis()}.$extension"
            val bucket = supabaseClient.storage.from(AVATARS_BUCKET)
            bucket.upload(path = fileName, data = imageBytes) {
                upsert = true
            }
            bucket.publicUrl(fileName)
        }
    }

    suspend fun uploadRestaurantBanner(restaurantId: String, imageBytes: ByteArray, extension: String = "jpg"): Result<String> {
        return runCatching {
            val cleanId = restaurantId.ifBlank { UUID.randomUUID().toString() }
            val fileName = "$cleanId/banner_${System.currentTimeMillis()}.$extension"
            val bucket = supabaseClient.storage.from(RESTAURANT_BANNERS_BUCKET)
            bucket.upload(path = fileName, data = imageBytes) {
                upsert = true
            }
            bucket.publicUrl(fileName)
        }
    }
}
