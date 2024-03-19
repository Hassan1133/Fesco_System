package com.example.fesco.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.databinding.ActivityLsmainBinding
import com.example.fesco.fragments.LSComplaintFragment
import com.example.fesco.fragments.LSLMFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

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
        loadFragment(LSComplaintFragment())
        bottomNavigationSelection()
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

        FirebaseAuth.getInstance().signOut()

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

    private fun bottomNavigationSelection() {
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lsComplaints ->
                    loadFragment(LSComplaintFragment())

                R.id.lm ->
                    loadFragment(LSLMFragment())
            }
            true
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.lsFrame, fragment).commit()
            when (fragment) {
                is LSComplaintFragment -> {
                    if (!binding.bottomNavigation.menu[0].isChecked) {
                        binding.bottomNavigation.menu[0].isChecked = true
                    }
                }

                is LSLMFragment -> {
                    if (!binding.bottomNavigation.menu[1].isChecked) {
                        binding.bottomNavigation.menu[1].isChecked = true
                    }
                }
            }
        }
    }
}