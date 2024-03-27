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

        // Handler to delay execution and navigate to the appropriate activity
        Handler(Looper.getMainLooper()).postDelayed({

            // Retrieve flags indicating user type from SharedPreferences
            val pref = getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
            val userCheck = pref.getBoolean("userFlag", false)
            val xenCheck = pref.getBoolean("xenFlag", false)
            val sdoCheck = pref.getBoolean("sdoFlag", false)
            val lsCheck = pref.getBoolean("lsFlag", false)
            val lmCheck = pref.getBoolean("lmFlag", false)

            // Determine the activity to navigate based on user type
            val intent = when {
                userCheck -> Intent(this, UserMainActivity::class.java)
                xenCheck -> Intent(this, XENMainActivity::class.java)
                sdoCheck -> Intent(this, SDOMainActivity::class.java)
                lsCheck -> Intent(this, LSMainActivity::class.java)
                lmCheck -> Intent(this, LMMainActivity::class.java)
                else -> Intent(
                    this,
                    LoginActivity::class.java
                ) // Default to LoginActivity if no flags are set
            }

            // Start the determined activity and finish the SplashActivity
            startActivity(intent)
            finish()

        }, 3000) // Delay for 3 seconds before executing the code inside the Handler
    }
}
