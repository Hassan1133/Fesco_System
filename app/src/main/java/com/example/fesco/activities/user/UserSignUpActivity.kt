package com.example.fesco.activities.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.activities.common.LoginActivity
import com.example.fesco.databinding.ActivityUserSignUpBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserSignUpActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserSignUpBinding
    private lateinit var usersRef: String // Variable to hold the reference to the users collection
    private lateinit var db: FirebaseFirestore // Firestore instance
    private lateinit var loadingDialog: Dialog // Dialog for loading indicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init() // Initialize activity components
    }

    private fun init() {
        binding.signInTxt.setOnClickListener(this) // Set click listener for sign-in text
        binding.signUpBtn.setOnClickListener(this) // Set click listener for sign-up button
        usersRef = "Users" // Set the reference to the users collection
        db = Firebase.firestore // Initialize Firestore database instance
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signInTxt -> {
                // Navigate to login activity when sign-in text is clicked
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.signUpBtn -> {
                // Check network connection before sign-up
                val networkManager = NetworkManager(this@UserSignUpActivity)
                val isConnected = networkManager.isNetworkAvailable()
                if (isConnected) {
                    // Validate user input data before sign-up
                    if (isDataValid()) {
                        loadingDialog = LoadingDialog.showLoadingDialog(this)!!
                        isConsumerExists() // Check if consumer ID exists in the database
                    }
                } else {
                    Toast.makeText(
                        this@UserSignUpActivity, "Please connect to the internet", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun isConsumerExists() {
        // Check if the consumer ID exists in the database
        db.collection(usersRef).document(binding.consumerNo.text.toString()).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.contains("ls") && document.contains("sdo") && document.contains("xen")) {
                    // If consumer ID exists and contains required fields, proceed with sign-up
                    signup(document.getString("ls")!!, document.getString("sdo")!!, document.getString("xen")!!)
                } else {
                    // If consumer ID is invalid or incomplete, show error message
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(this, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // If an error occurs while fetching data from Firestore, show error message
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun signup(ls: String, sdo: String, xen: String) {
        // Perform user sign-up by adding user data to Firestore
        val user = UserModel().apply {
            consumerID = binding.consumerNo.text.toString()
            name = binding.name.text.toString()
            phoneNo = binding.phoneNo.text.toString()
            address = binding.address.text.toString()
            this.ls = ls
            key = binding.password.text.toString()
        }
        db.collection(usersRef).document(binding.consumerNo.text.toString()).set(user)
            .addOnSuccessListener {
                goToLoginActivity() // Navigate to login activity after successful sign-up
            }
            .addOnFailureListener { e ->
                // If sign-up fails, show error message
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToLoginActivity() {
        // Navigate to login activity after successful sign-up
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(this, "Signed Up Successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isDataValid(): Boolean {
        // Validate user input data
        var valid = true
        if (binding.consumerNo.text.isNullOrEmpty() || binding.consumerNo.text!!.length < 10) {
            binding.consumerNo.error = "Please enter a valid consumer number"
            valid = false
        }
        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter a valid name"
            valid = false
        }
        if (binding.phoneNo.text.isNullOrEmpty()) {
            binding.phoneNo.error = "Please enter a valid phone number"
            valid = false
        }
        if (binding.address.text.isNullOrEmpty()) {
            binding.address.error = "Please enter a valid address"
            valid = false
        }
        if (binding.password.text.isNullOrEmpty() || binding.password.text!!.length < 6) {
            binding.password.error = "Please enter a valid password (minimum 6 characters)"
            valid = false
        }
        return valid
    }
}
