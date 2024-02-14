package com.example.fesco.activities

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init() {
        val adp = ViewPagerAdp(supportFragmentManager, lifecycle)
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = adp
        val tabs: TabLayout = binding.tabsLayout
        TabLayoutMediator(tabs, viewPager) { tab, position ->
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