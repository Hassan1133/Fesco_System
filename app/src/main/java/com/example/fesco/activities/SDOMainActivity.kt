package com.example.fesco.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.databinding.ActivitySdomainBinding
import com.example.fesco.fragments.SDOComplaintFragment
import com.example.fesco.fragments.SDOLSFragment
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
        loadFragment(SDOComplaintFragment())
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

        val pref = getSharedPreferences("login", MODE_PRIVATE)
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
                R.id.complaints -> {
                    loadFragment(SDOComplaintFragment())
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