package com.example.fesco.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fesco.R
import com.example.fesco.activities.ls.LSMainActivity
import com.example.fesco.activities.common.LoginActivity
import com.example.fesco.activities.lm.LMMainActivity
import com.example.fesco.activities.sdo.SDOMainActivity
import com.example.fesco.activities.xen.XENMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMNotificationService : FirebaseMessagingService() {

    // Notification channel ID and name
    private val channelId = "FescoComplaintsChannel"
    private val channelName = "Fesco Complaints"

    // Called when a new FCM message is received
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Check if the user is authenticated
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Extract title, body, and user type from the message data
            val title = message.getData()["title"]
            val body = message.getData()["body"]
            val userType = message.getData()["userType"]

            // Send notification if user is authenticated
            sendNotification(title!!, body!!, userType!!)
        }
    }

    // Function to send notification
    private fun sendNotification(title: String, messageBody: String, userType: String) {
        // Create an intent based on the user type to open the appropriate activity
        val intent = Intent(this, getMainActivityClass(userType)).apply {
            // Pass extra data to the activity
            putExtra("notificationFragment", "NotResolvedComplaintFragment")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Create a PendingIntent to open the activity when notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setSmallIcon(R.drawable.complaint)
            setContentTitle(title)
            setContentText(messageBody)
            setDefaults(Notification.DEFAULT_SOUND)
            setAutoCancel(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(pendingIntent)
        }

        // Get the system's NotificationManager service
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build())
    }

    // Function to get the appropriate MainActivity class based on user type
    private fun getMainActivityClass(userType: String): Class<*> {
        return when (userType) {
            "userToLs" -> LSMainActivity::class.java
            "lsToLm" -> LMMainActivity::class.java
            "lsToSdo" -> SDOMainActivity::class.java
            "sdoToXen" -> XENMainActivity::class.java
            else -> LoginActivity::class.java
        }
    }
}