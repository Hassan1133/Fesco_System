package com.example.fesco.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.databinding.ActivityXenmainBinding
import com.example.fesco.fragments.XENComplaintFragment
import com.example.fesco.fragments.XENSDOFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class XENMainActivity : AppCompatActivity() , OnClickListener{

    private lateinit var binding: ActivityXenmainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXenmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        setXENName()
        bottomNavigationSelection()
        loadFragment(XENComplaintFragment())
    }

    private fun setXENName() {
        val xenData = getSharedPreferences("xenData", MODE_PRIVATE)
        binding.name.text = xenData.getString("name", "")
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
                val intent = Intent(this, XENProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun bottomNavigationSelection() {
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.complaints -> {
                    loadFragment(XENComplaintFragment())
                    return@OnItemSelectedListener true
                }

                R.id.sdo -> {
                    loadFragment(XENSDOFragment())
                    return@OnItemSelectedListener true
                }
            }
            false
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        setXENName()
    }

    private fun logOut() {

        FirebaseAuth.getInstance().signOut()

        val xenData = getSharedPreferences("xenData", MODE_PRIVATE)
        val profileDataEditor = xenData.edit()
        profileDataEditor.clear()
        profileDataEditor.apply()

        val pref = getSharedPreferences("login", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("xenFlag", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}