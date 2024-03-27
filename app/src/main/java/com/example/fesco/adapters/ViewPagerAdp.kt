package com.example.fesco.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fesco.fragments.lm.LMLoginFragment
import com.example.fesco.fragments.ls.LSLoginFragment
import com.example.fesco.fragments.sdo.SDOLoginFragment
import com.example.fesco.fragments.user.UserLoginFragment
import com.example.fesco.fragments.xen.XENLoginFragment

class ViewPagerAdp(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    // Returns the total number of fragments in the ViewPager
    override fun getItemCount(): Int {
        return 5 // Assuming there are 5 fragments
    }

    // Creates and returns the fragment at the specified position
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserLoginFragment()   // Fragment for user login
            1 -> XENLoginFragment()    // Fragment for XEN login
            2 -> SDOLoginFragment()   // Fragment for SDO login
            3 -> LSLoginFragment()    // Fragment for LS login
            4 -> LMLoginFragment()    // Fragment for LM login
            else -> UserLoginFragment() // Default: user login fragment
        }
    }
}
