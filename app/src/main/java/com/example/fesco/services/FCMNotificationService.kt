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

    private val channelId = "FescoComplaintsChannel"
    private val channelName = "Fesco Complaints"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (FirebaseAuth.getInstance().currentUser != null) {
            val title = message.getData()["title"]
            val body = message.getData()["body"]
            val userType = message.getData()["userType"]
            sendNotification(title!!, body!!, userType!!)
        }
    }

    private fun sendNotification(title: String, messageBody: String, userType: String) {
        val intent = Intent(this, getMainActivityClass(userType)).apply {
            putExtra("notificationFragment", "NotResolvedComplaintFragment")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

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

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

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