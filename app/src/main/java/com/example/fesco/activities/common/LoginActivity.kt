package com.example.fesco.activities.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.fesco.adapters.ViewPagerAdp
import com.example.fesco.databinding.ActivityLoginBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        // Initialize the activity components
        init()
        // Set the content view to the root layout of the inflated binding
        setContentView(binding.root)
    }

    private fun init() {
        // Create adapter for ViewPager
        val adapter = ViewPagerAdp(supportFragmentManager, lifecycle)
        // Get reference to ViewPager
        val viewPager: ViewPager2 = binding.viewPager
        // Set adapter to ViewPager
        viewPager.adapter = adapter
        // Get reference to TabLayout
        val tabs: TabLayout = binding.tabsLayout
        // Attach TabLayout to ViewPager
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            // Set tab text based on position
            when (position) {
                0 -> tab.text = "USER"
                1 -> tab.text = "XEN"
                2 -> tab.text = "SDO"
                3 -> tab.text = "LS"
                4 -> tab.text = "LM"
            }
        }.attach()
    }
}
