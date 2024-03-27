package com.example.fesco.activities.sdo

import android.Manifest
import android.content.Intent
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
import com.example.fesco.databinding.ActivitySdomainBinding
import com.example.fesco.fragments.sdo.SDOLSFragment
import com.example.fesco.fragments.sdo.SDONotResolvedComplaintFragment
import com.example.fesco.fragments.sdo.SDOResolvedComplaintFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth


class SDOMainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivitySdomainBinding // View binding instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdomainBinding.inflate(layoutInflater) // Initialize view binding
        setContentView(binding.root) // Set content view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        init() // Initialize activity components
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        binding.logoutBtn.setOnClickListener(this) // Set click listener for logout button
        binding.profile.setOnClickListener(this) // Set click listener for profile button
        setSDOName() // Set SDO name
        bottomNavigationSelection() // Handle bottom navigation selection
        loadFragment(SDONotResolvedComplaintFragment()) // Load initial fragment
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

    private fun setSDOName() {
        val sdoData = getSharedPreferences("sdoData", MODE_PRIVATE)
        binding.name.text = sdoData.getString("name", "")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> {
                // Logout confirmation dialog
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
                // Open SDO profile activity
                val intent = Intent(this, SDOProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun logOut() {
        // Perform logout operation
        FirebaseAuth.getInstance().signOut()

        // Clear SDO data from SharedPreferences
        getSharedPreferences("sdoData", MODE_PRIVATE).edit().clear().apply()

        // Set SDO flag to false in SharedPreferences
        getSharedPreferences("fescoLogin", MODE_PRIVATE).edit().putBoolean("sdoFlag", false).apply()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun bottomNavigationSelection() {
        // Handle bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sdoUserNotResolvedComplaints -> {
                    loadFragment(SDONotResolvedComplaintFragment())
                    return@OnItemSelectedListener true
                }
                R.id.sdoUserResolvedComplaints -> {
                    loadFragment(SDOResolvedComplaintFragment())
                    return@OnItemSelectedListener true
                }
                R.id.ls -> {
                    loadFragment(SDOLSFragment())
                    return@OnItemSelectedListener true
                }
            }
            false
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        // Load fragment into the container
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.sdoFrame, fragment).commit()
        }
    }
}
