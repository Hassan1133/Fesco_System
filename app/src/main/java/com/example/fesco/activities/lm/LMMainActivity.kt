package com.example.fesco.activities.lm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.activities.common.LoginActivity
import com.example.fesco.databinding.ActivityLmmainBinding
import com.example.fesco.fragments.lm.LMNotResolvedComplaintFragment
import com.example.fesco.fragments.lm.LMResolvedComplaintFragment
import com.example.fesco.fragments.ls.LSLMFragment
import com.example.fesco.fragments.ls.LSNotResolvedComplaintFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth


class LMMainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLmmainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLmmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setLMName()
        loadFragment(LMNotResolvedComplaintFragment())
        bottomNavigationSelection()
    }

    private fun setLMName() {
        val lmData = getSharedPreferences("lmData", MODE_PRIVATE)
        binding.name.text = lmData.getString("name", "")
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
                val intent = Intent(this, LMProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun logOut() {

        FirebaseAuth.getInstance().signOut()

        val lmData = getSharedPreferences("lmData", MODE_PRIVATE)
        val profileDataEditor = lmData.edit()
        profileDataEditor.clear()
        profileDataEditor.apply()

        val pref = getSharedPreferences("fescoLogin", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("lmFlag", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun bottomNavigationSelection() {
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lmUnresolvedComplaints ->
                    loadFragment(LMNotResolvedComplaintFragment())

                R.id.lmResolvedComplaints ->
                    loadFragment(LMResolvedComplaintFragment())
            }
            true
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.lmFrame, fragment).commit()
            when (fragment) {
                is LSNotResolvedComplaintFragment -> {
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