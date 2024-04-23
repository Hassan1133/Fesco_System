package com.example.fesco.activities.xen

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
import com.example.fesco.databinding.ActivityXenmainBinding
import com.example.fesco.fragments.xen.XENAnalyticsFragment
import com.example.fesco.fragments.xen.XENNotResolvedComplaintFragment
import com.example.fesco.fragments.xen.XENResolvedComplaintFragment
import com.example.fesco.fragments.xen.XENSDOFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class XENMainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityXenmainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXenmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        init() // Initialize activity components
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        // Initialize UI components and functionality
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setXENName() // Set XEN's name
        bottomNavigationSelection() // Set up bottom navigation
        loadFragment(XENNotResolvedComplaintFragment()) // Load initial fragment
        checkNotificationPermission() // Check notification permission
    }

    // Register launcher for permission request
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
        // Check notification permission and request if needed
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with your action
        } else {
            // Permission not granted, request it
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

    // Show rationale for permission request
    private fun showPermissionRationale(permission: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage("This app needs notification permission to...") // Provide reason
            .setPositiveButton("Grant") { _, _ -> launcher.launch(permission) }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setXENName() {
        // Set XEN's name from SharedPreferences
        val xenData = getSharedPreferences("xenData", MODE_PRIVATE)
        binding.name.text = xenData.getString("name", "")
    }

    override fun onClick(v: View?) {
        // Handle click events for logout and profile button
        when (v?.id) {
            R.id.logoutBtn -> {
                showLogoutDialog() // Show confirmation dialog for logout
            }

            R.id.profile -> {
                // Open XEN profile activity
                val intent = Intent(this, XENProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showLogoutDialog() {
        // Show confirmation dialog for logout
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.logout_message)
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> logOut() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun bottomNavigationSelection() {
        // Handle bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.xenUserNotResolvedComplaints -> {
                    loadFragment(XENNotResolvedComplaintFragment()) // Load not resolved complaints fragment
                    return@OnItemSelectedListener true
                }
                R.id.xenUserResolvedComplaints -> {
                    loadFragment(XENResolvedComplaintFragment()) // Load resolved complaints fragment
                    return@OnItemSelectedListener true
                }
                R.id.sdo -> {
                    loadFragment(XENSDOFragment()) // Load SDO fragment
                    return@OnItemSelectedListener true
                }

                R.id.analytics -> {
                    loadFragment(XENAnalyticsFragment()) // Load SDO fragment
                    return@OnItemSelectedListener true
                }
            }
            false
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        // Load fragment into the container
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.xenFrame, fragment).commit()
        }
    }

    private fun logOut() {
        // Handle logout functionality
        FirebaseAuth.getInstance().signOut() // Sign out from Firebase authentication

        // Clear XEN data from SharedPreferences
        getSharedPreferences("xenData", MODE_PRIVATE).edit().clear().apply()

        // Set XEN flag to false in SharedPreferences
        getSharedPreferences("fescoLogin", MODE_PRIVATE).edit().putBoolean("xenFlag", false).apply()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
