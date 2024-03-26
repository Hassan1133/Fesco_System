package com.example.fesco.activities.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.activities.lm.LMMainActivity
import com.example.fesco.activities.ls.LSMainActivity
import com.example.fesco.activities.sdo.SDOMainActivity
import com.example.fesco.activities.user.UserMainActivity
import com.example.fesco.activities.xen.XENMainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            val pref = getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
            val userCheck = pref.getBoolean("userFlag", false)
            val xenCheck = pref.getBoolean("xenFlag", false)
            val sdoCheck = pref.getBoolean("sdoFlag", false)
            val lsCheck = pref.getBoolean("lsFlag", false)
            val lmCheck = pref.getBoolean("lmFlag", false)

            val intent = when {
                userCheck -> Intent(this, UserMainActivity::class.java)
                xenCheck -> Intent(this, XENMainActivity::class.java)
                sdoCheck -> Intent(this, SDOMainActivity::class.java)
                lsCheck -> Intent(this, LSMainActivity::class.java)
                lmCheck -> Intent(this, LMMainActivity::class.java)
                else -> Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()

        },3000)
    }
}