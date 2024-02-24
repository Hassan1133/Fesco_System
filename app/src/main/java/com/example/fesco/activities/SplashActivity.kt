package com.example.fesco.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            val pref = getSharedPreferences("login", MODE_PRIVATE)
            val userCheck = pref.getBoolean("userFlag", false)
            val xenCheck = pref.getBoolean("xenFlag", false)
            val sdoCheck = pref.getBoolean("sdoFlag", false)
//            val lsCheck = pref.getBoolean("lsFlag", false)
//            val lmCheck = pref.getBoolean("lmFlag", false)
            var intent: Intent

            if (userCheck) {
                intent = Intent(this, UserMainActivity::class.java)
            }
            else if (xenCheck) {
                intent = Intent(this, XENMainActivity::class.java)
            }
            else if (sdoCheck) {
                intent = Intent(this, SDOMainActivity::class.java)
            }
            else {
                intent = Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()

        },3000)
    }
}