package com.example.fesco.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserSignUpBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserSignUpActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserSignUpBinding

    private lateinit var usersRef: String

    // Initialize firestore
    private lateinit var db : FirebaseFirestore

    private lateinit var loadingDialog : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignUpBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init() {
        binding.signInTxt.setOnClickListener(this)
        binding.signUpBtn.setOnClickListener(this)
        usersRef = "Users"
        // Initialize firestore
        db = Firebase.firestore
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
                    loadingDialog = LoadingDialog.showLoadingDialog(this)!!
                    isConsumerExists()
                }
            }
        }
    }

    private fun isConsumerExists() {
        db.collection(usersRef).document(binding.consumerNo.text.toString()).get()
            .addOnSuccessListener {
                if (it.exists() && !it.contains("consumerID") && it.contains("ls") && it.contains("sdo") && it.contains("xen")) {
                    signup(it.getString("ls")!!, it.getString("sdo")!!, it.getString("xen")!!)
                } else {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(this, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun signup(ls: String, sdo: String, xen: String) {
        val user = User()
        user.consumerID = binding.consumerNo.text.toString()
        user.name = binding.name.text.toString()
        user.phoneNo = binding.phoneNo.text.toString()
        user.address = binding.address.text.toString()
        user.ls = ls
        user.sdo = sdo
        user.xen = xen
        user.key = binding.password.text.toString()
        db.collection(usersRef).document(binding.consumerNo.text.toString()).set(user)
            .addOnSuccessListener {
                goToLoginActivity()
            }
            .addOnFailureListener{
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToLoginActivity(){
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(this, "Signed Up Successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isDataValid(): Boolean {
        var valid = true
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