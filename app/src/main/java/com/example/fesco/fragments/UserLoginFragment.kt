package com.example.fesco.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.example.fesco.R
import com.example.fesco.activities.LoginActivity
import com.example.fesco.activities.UserSignUpActivity
import com.example.fesco.databinding.FragmentUserLoginBinding

class UserLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserLoginBinding;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.signUpTxt.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signUpTxt -> {
                val intent = Intent(activity, UserSignUpActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
    }

}