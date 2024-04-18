package com.example.fesco.activities.lm

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.example.fesco.R
import com.example.fesco.databinding.ActivityLmprofileBinding
import com.example.fesco.databinding.UserEditPasswordDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LMProfileActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityLmprofileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var userEditPasswordDialogBinding: UserEditPasswordDialogBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var userEditPasswordDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout
        binding = ActivityLmprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize activity components
        init()
    }

    private fun init() {
        // Load profile data, get current Firebase user, and set click listener
        getProfileDataFromSharedPreferences()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        binding.editIcon.setOnClickListener(this)
    }

    private fun getProfileDataFromSharedPreferences() {
        // Retrieve LM profile data from SharedPreferences and set it to UI elements
        val lmData = getSharedPreferences("lmData", MODE_PRIVATE)
        binding.name.text = lmData.getString("name", "")
        binding.email.text = lmData.getString("email", "")
        binding.city.text = lmData.getString("city", "")
        binding.subDivision.text = lmData.getString("subDivision", "")
    }

    private fun createPasswordDialog() {
        // Create and show password update dialog
        userEditPasswordDialogBinding = UserEditPasswordDialogBinding.inflate(LayoutInflater.from(this))
        userEditPasswordDialog = Dialog(this)
        userEditPasswordDialog.setContentView(userEditPasswordDialogBinding.root)
        userEditPasswordDialog.setCancelable(false)
        userEditPasswordDialog.show()
        userEditPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        userEditPasswordDialogBinding.updateBtn.setOnClickListener {
            // When update button is clicked, verify current password and update if valid
            if (isValidPassword()) {
                loadingDialog = LoadingDialog.showLoadingDialog(this)!!
                verifyUserCurrentPassword(firebaseUser.email!!, userEditPasswordDialogBinding.userCurrentPassword.text.toString())
            }
        }

        userEditPasswordDialogBinding.closeBtn.setOnClickListener{
            // Close the password dialog
            userEditPasswordDialog.dismiss()
        }
    }

    private fun verifyUserCurrentPassword(email: String, password: String) {
        // Verify user's current password
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    // If password is correct, update the password
                    updateUserPassword(userEditPasswordDialogBinding.userNewPassword.text.toString())
                }
            }.addOnFailureListener { e ->
                // Handle authentication failure
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userEditPasswordDialogBinding.userCurrentPassword.error = "Password is invalid"
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserPassword(newPassword: String) {
        // Update user's password
        firebaseUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password updated successfully
                    Toast.makeText(this, "User Password Updated Successfully", Toast.LENGTH_SHORT).show()
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    userEditPasswordDialog.dismiss()
                }
            }.addOnFailureListener { e ->
                // Handle password update failure
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View) {
        // Handle click events
        when (v.id) {
            R.id.editIcon -> {
                // Show password update dialog when edit icon is clicked
                createPasswordDialog()
            }
        }
    }

    private fun isValidPassword(): Boolean {
        // Validate password length
        var valid = true

        if (userEditPasswordDialogBinding.userCurrentPassword.text!!.length < 6) {
            userEditPasswordDialogBinding.userCurrentPassword.error = "Enter a valid password"
            valid = false
        }

        if (userEditPasswordDialogBinding.userNewPassword.text!!.length < 6) {
            userEditPasswordDialogBinding.userNewPassword.error = "Enter a valid password"
            valid = false
        }

        return valid
    }

}