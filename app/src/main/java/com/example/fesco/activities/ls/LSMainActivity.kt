package com.example.fesco.activities.ls

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.activities.common.LoginActivity
import com.example.fesco.databinding.ActivityLsmainBinding
import com.example.fesco.fragments.ls.LSLMFragment
import com.example.fesco.fragments.ls.LSNotResolvedComplaintFragment
import com.example.fesco.fragments.ls.LSResolvedComplaintFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class LSMainActivity : AppCompatActivity() , View.OnClickListener {

    private lateinit var binding: ActivityLsmainBinding // Binding for the activity layout

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLsmainBinding.inflate(layoutInflater) // Inflate the activity layout
        setContentView(binding.root) // Set the content view
        init() // Initialize the activity components
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        binding.logoutBtn.setOnClickListener(this) // Set click listener for logout button
        binding.profile.setOnClickListener(this) // Set click listener for profile button
        setLsName() // Set LS name from SharedPreferences
        loadFragment(LSNotResolvedComplaintFragment()) // Load the default fragment
        bottomNavigationSelection() // Setup bottom navigation
        checkNotificationPermission() // Check and request notification permission if necessary
    }

    // Register for permission result using ActivityResultContracts
    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with your action
        } else {
            // Permission denied or forever denied, handle accordingly
        }
    }

    // Check notification permission and request if necessary
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

    // Show rationale for permission
    private fun showPermissionRationale(permission: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage("This app needs notification permission to...") // Provide reason
            .setPositiveButton("Grant") { _, _ -> launcher.launch(permission) }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Set LS name from SharedPreferences
    private fun setLsName() {
        val lsData = getSharedPreferences("lsData", MODE_PRIVATE).getString("name", "")
        binding.name.text = lsData
    }

    // Handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> {
                // Show logout confirmation dialog
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.logout_message)
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ -> logOut() }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            R.id.profile -> {
                // Navigate to profile activity
                val intent = Intent(this, LSProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // Logout the user
    private fun logOut() {
        FirebaseAuth.getInstance().signOut()

        // Clear LS data from SharedPreferences
        getSharedPreferences("lsData", MODE_PRIVATE).edit().clear().apply()

        // Update login flag in SharedPreferences
        getSharedPreferences("fescoLogin", MODE_PRIVATE).edit().putBoolean("lsFlag", false).apply()

        // Navigate to login activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Setup bottom navigation
    private fun bottomNavigationSelection() {
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lsUnResolvedComplaints -> {
                    // Load the unresolved complaints fragment
                    loadFragment(LSNotResolvedComplaintFragment())
                    return@OnItemSelectedListener true
                }

                R.id.lsResolvedComplaints -> {
                    // Load the resolved complaints fragment
                    loadFragment(LSResolvedComplaintFragment())
                    return@OnItemSelectedListener true
                }

                R.id.lm -> {
                    // Load the LM fragment
                    loadFragment(LSLMFragment())
                    return@OnItemSelectedListener true
                }

                else -> {
                    // Handle unexpected item selection (optional)
                    return@OnItemSelectedListener false
                }
            }
        })
    }

    // Load fragment into the container
    private fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.lsFrame, fragment)
                .commit()
        }
    }
}
