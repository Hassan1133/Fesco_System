package com.example.fesco.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.example.fesco.R
import com.example.fesco.activities.LoginActivity
import com.example.fesco.activities.UserMainActivity
import com.example.fesco.activities.UserSignUpActivity
import com.example.fesco.databinding.FragmentUserLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserLoginBinding;

    private val usersRef: String = "Users"

    // Initialize firestore
    private val db = Firebase.firestore
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
        binding.loginBtn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signUpTxt -> {
                val intent = Intent(activity, UserSignUpActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            R.id.loginBtn -> {
                if (isDataValid()) {
                    signIn()
                }
            }
        }
    }

    private fun isDataValid(): Boolean {
        var valid: Boolean = true
        if (binding.consumerNo.text.isNullOrEmpty() || binding.consumerNo.text!!.length < 10) {
            binding.consumerNo.error = "Please enter valid consumer number"
            valid = false
        }
        if (binding.password.text.isNullOrEmpty() || binding.password.text!!.length < 6) {
            binding.password.error = "Please enter valid password"
            valid = false
        }
        return valid
    }

    private fun signIn() {
        db.collection(usersRef).document(binding.consumerNo.text.toString()).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    if (it.getString("consumerID") == binding.consumerNo.text.toString()) {
                        if (it.getString("key") == binding.password.text.toString()) {
                            Toast.makeText(activity, "Logged in successfully", Toast.LENGTH_SHORT)
                                .show()
                            goToUserMainActivity();
                        } else {
                            Toast.makeText(activity, "Incorrect password", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(activity, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToUserMainActivity() {
        val intent : Intent = Intent(activity, UserMainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}