package com.example.fesco.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserSignUpBinding
import com.example.fesco.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserSignUpActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserSignUpBinding

    private val usersRef: String = "Users"

    // Initialize firestore
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignUpBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init() {
        binding.signInTxt.setOnClickListener(this)
        binding.signUpBtn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signInTxt -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.signUpBtn -> {
                if (isDataValid()) {
                    isConsumerExists()
                }
            }
        }
    }

    private fun isConsumerExists() {
        db.collection(usersRef).document(binding.consumerNo.text.toString()).get()
            .addOnSuccessListener {
                if (it.exists() && !it.contains("consumerID")) {
                    signup()
                } else {
                    Toast.makeText(this, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun signup() {
        val user = User(
            binding.consumerNo.text.toString(),
            binding.name.text.toString(),
            binding.phoneNo.text.toString(),
            binding.address.text.toString(),
            binding.password.text.toString()
        )
        db.collection(usersRef).document(binding.consumerNo.text.toString()).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Signed Up Successfully", Toast.LENGTH_SHORT).show()
                val intent : Intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun isDataValid(): Boolean {
        var valid: Boolean = true
        if (binding.consumerNo.text.isNullOrEmpty() || binding.consumerNo.text!!.length < 10) {
            binding.consumerNo.error = "Please enter valid consumer number"
            valid = false
        }
        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter valid name"
            valid = false
        }
        if (binding.phoneNo.text.isNullOrEmpty()) {
            binding.phoneNo.error = "Please enter valid phone number"
            valid = false
        }
        if (binding.address.text.isNullOrEmpty()) {
            binding.address.error = "Please enter valid address"
            valid = false
        }
        if (binding.password.text.isNullOrEmpty() || binding.password.text!!.length < 6) {
            binding.password.error = "Please enter valid password"
            valid = false
        }
        return valid
    }
}