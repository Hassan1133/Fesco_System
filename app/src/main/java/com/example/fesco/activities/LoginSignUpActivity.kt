package com.example.fesco.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.fesco.R
import com.example.fesco.adapters.ViewPagerAdp
import com.example.fesco.databinding.ActivityLoginSignUpBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginSignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSignUpBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init() {
        val adp = ViewPagerAdp(supportFragmentManager, lifecycle)
        val viewPager : ViewPager2 = binding.viewPager
        viewPager.adapter = adp
        val tabs : TabLayout = binding.tabsLayout
        TabLayoutMediator(tabs, viewPager){ tab, position ->
            when(position)
            {
                0 -> tab.text = "USER"
                1 -> tab.text = "XEN"
                2 -> tab.text = "SDO"
                3 -> tab.text = "LS"
                4 -> tab.text = "LM"
            }
        }.attach()
    }
}