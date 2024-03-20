package com.example.fesco.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fesco.services.SendComplaintsToSDOService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, SendComplaintsToSDOService::class.java)
        context.startService(serviceIntent)
    }
}
