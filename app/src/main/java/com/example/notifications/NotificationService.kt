package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

class NotificationService(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about your order status"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showOrderStatusNotification(orderId: String, statusMessage: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // fallback icon
            .setContentTitle("Order Update")
            .setContentText(statusMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(orderId.hashCode(), notification)
    }
    
    companion object {
        const val CHANNEL_ID = "order_status_channel"
    }
}
