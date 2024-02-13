package com.example.fesco.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fesco.fragments.LMLoginFragment
import com.example.fesco.fragments.LSLoginFragment
import com.example.fesco.fragments.SDOLoginFragment
import com.example.fesco.fragments.UserLoginFragment
import com.example.fesco.fragments.XENLoginFragment

class ViewPagerAdp (fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when(position)
        {
            0 -> UserLoginFragment()
            1 -> XENLoginFragment()
            2 -> SDOLoginFragment()
            3 -> LSLoginFragment()
            4 -> LMLoginFragment()
            else -> UserLoginFragment()
        }
    }
}