package com.example.fesco.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserMainBinding
import com.example.fesco.databinding.ComplaintDialogBinding
import com.example.fesco.fragments.LSComplaintFragment
import com.example.fesco.fragments.LSLMFragment
import com.example.fesco.fragments.UserPendingComplaintsFragment
import com.example.fesco.fragments.UserResolvedComplaintsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView

class UserMainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserMainBinding

    private lateinit var userData: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setUserName()
        bottomNavigationSelection()
        loadFragment(UserPendingComplaintsFragment())
    }

    private fun setUserName() {
        userData = getSharedPreferences("userData", MODE_PRIVATE)
        binding.name.text = userData.getString("name", "")
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> showLogoutDialog()

            R.id.profile -> startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this).setMessage(R.string.logout_message).setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> logOut() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun logOut() {

        val userData = getSharedPreferences("userData", MODE_PRIVATE)
        val profileDataEditor = userData.edit()
        profileDataEditor.clear()
        profileDataEditor.apply()

        val pref = getSharedPreferences("login", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("userFlag", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun bottomNavigationSelection() {
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.userPendingComplaints ->
                    loadFragment(UserPendingComplaintsFragment())

                R.id.userResolvedComplaints ->
                    loadFragment(UserResolvedComplaintsFragment())
            }
            true
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.userFrame, fragment).commit()
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