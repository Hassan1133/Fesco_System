package com.example.fesco.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.fesco.R
import com.example.fesco.databinding.ActivityLsmainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LSMainActivity : AppCompatActivity() , View.OnClickListener {

    private lateinit var binding: ActivityLsmainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLsmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setLsName()
    }

    private fun setLsName() {
        val lsData = getSharedPreferences("lsData", MODE_PRIVATE)
        binding.name.text = lsData.getString("name", "")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> {
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.logout_message)
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        logOut()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            R.id.profile -> {
                val intent = Intent(this, LSProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun logOut() {

        val lsData = getSharedPreferences("lsData", MODE_PRIVATE)
        val profileDataEditor = lsData.edit()
        profileDataEditor.clear()
        profileDataEditor.apply()

        val pref = getSharedPreferences("login", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("lsFlag", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}