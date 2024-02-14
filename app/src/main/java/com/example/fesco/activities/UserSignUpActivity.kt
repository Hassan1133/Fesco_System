package com.example.fesco.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserSignUpBinding

class UserSignUpActivity : AppCompatActivity() , OnClickListener{

    private lateinit var binding: ActivityUserSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignUpBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init()
    {
        binding.signInTxt.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signInTxt -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}