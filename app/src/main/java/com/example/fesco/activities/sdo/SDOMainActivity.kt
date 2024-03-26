package com.example.fesco.activities.sdo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var binding: ActivitySdomainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdomainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setSDOName()
        bottomNavigationSelection()
        loadFragment(SDONotResolvedComplaintFragment())
        checkNotificationPermission()
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

    private fun checkNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, proceed with your action
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show rationale to the user, then request permission using launcher
            } else {
                // Request permission directly using launcher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setSDOName() {
        val sdoData = getSharedPreferences("sdoData", MODE_PRIVATE)
        binding.name.text = sdoData.getString("name", "")
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
                val intent = Intent(this, SDOProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun logOut() {

        FirebaseAuth.getInstance().signOut()

        val sdoData = getSharedPreferences("sdoData", MODE_PRIVATE)
        val profileDataEditor = sdoData.edit()
        profileDataEditor.clear()
        profileDataEditor.apply()

        val pref = getSharedPreferences("fescoLogin", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("sdoFlag", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun bottomNavigationSelection() {
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
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.sdoFrame, fragment).commit()
        }
    }
}