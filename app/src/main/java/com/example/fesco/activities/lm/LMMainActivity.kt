package com.example.fesco.activities.lm

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
import com.example.fesco.databinding.ActivityLmmainBinding
import com.example.fesco.fragments.lm.LMNotResolvedComplaintFragment
import com.example.fesco.fragments.lm.LMResolvedComplaintFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth


class LMMainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLmmainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout and set content view
        binding = ActivityLmmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        // Initialize activity components
        init()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        // Set click listeners and initialize UI components
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setLMName() // Set LM name on UI
        loadFragment(LMNotResolvedComplaintFragment()) // Load initial fragment
        bottomNavigationSelection() // Set bottom navigation listener
        checkNotificationPermission() // Check notification permission
    }

    // Activity result launcher for permission request
    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denial
            // Consider showing a message or taking appropriate action
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


    private fun setLMName() {
        // Set LM name from SharedPreferences to UI
        val lmData = getSharedPreferences("lmData", MODE_PRIVATE)
        binding.name.text = lmData.getString("name", "")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> {
                // Show logout confirmation dialog
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.logout_message)
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        logOut() // Perform logout
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            R.id.profile -> {
                // Open profile activity
                val intent = Intent(this, LMProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun logOut() {
        // Perform logout actions
        FirebaseAuth.getInstance().signOut()
        getSharedPreferences("lmData", MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("fescoLogin", MODE_PRIVATE).edit().putBoolean("lmFlag", false).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish current activity after logout
    }

    private fun bottomNavigationSelection() {
        // Handle bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lmUnresolvedComplaints -> loadFragment(LMNotResolvedComplaintFragment())
                R.id.lmResolvedComplaints -> loadFragment(LMResolvedComplaintFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment?) {
        // Load fragment into the container
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.lmFrame, fragment).commit()
        }
    }
}