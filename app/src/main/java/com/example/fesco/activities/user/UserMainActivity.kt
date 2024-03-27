package com.example.fesco.activities.user

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.activities.common.LoginActivity
import com.example.fesco.databinding.ActivityUserMainBinding
import com.example.fesco.fragments.user.UserNotResolvedComplaintsFragment
import com.example.fesco.fragments.user.UserResolvedComplaintsFragment
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        init()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        // Set click listeners and initialize UI components
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setUserName() // Set the user's name on the UI
        bottomNavigationSelection() // Handle bottom navigation item selection
        loadFragment(UserNotResolvedComplaintsFragment()) // Load initial fragment
        checkNotificationPermission() // Check notification permission
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with your action
        } else {
            // Permission denied or forever denied, handle accordingly
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with your action
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show rationale to the user, then request permission using launcher
                showPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Request permission directly using launcher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationale(permission: String) {
        // Explain why the app needs permission
        MaterialAlertDialogBuilder(this)
            .setMessage("This app needs notification permission to...") // Provide reason
            .setPositiveButton("Grant") { _, _ -> launcher.launch(permission) }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setUserName() {
        // Set the user's name retrieved from SharedPreferences
        userData = getSharedPreferences("userData", MODE_PRIVATE)
        binding.name.text = userData.getString("name", "")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> showLogoutDialog() // Show logout confirmation dialog
            R.id.profile -> startActivity(Intent(this, UserProfileActivity::class.java)) // Open profile activity
        }
    }

    private fun showLogoutDialog() {
        // Show logout confirmation dialog
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.logout_message)
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> logOut() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun logOut() {
        // Clear user data and navigate to the login screen
        getSharedPreferences("userData", MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("fescoLogin", MODE_PRIVATE).edit().putBoolean("userFlag", false).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun bottomNavigationSelection() {
        // Handle bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.userPendingComplaints ->
                    loadFragment(UserNotResolvedComplaintsFragment()) // Load unresolved complaints fragment
                R.id.userResolvedComplaints ->
                    loadFragment(UserResolvedComplaintsFragment()) // Load resolved complaints fragment
            }
            true
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        // Load the specified fragment into the fragment container
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.userFrame, fragment).commit()
        }
    }
}
