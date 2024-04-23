package com.example.fesco.activities.sdo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.databinding.ActivitySdoprofileBinding
import com.example.fesco.databinding.UserEditPasswordDialogBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SDOProfileActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivitySdoprofileBinding // Binding for the activity layout

    private lateinit var firebaseUser: FirebaseUser // Firebase user object to manage user authentication

    private lateinit var userEditPasswordDialogBinding: UserEditPasswordDialogBinding // Binding for the password edit dialog

    private lateinit var userEditPasswordDialog: Dialog // Dialog for user password editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdoprofileBinding.inflate(layoutInflater) // Inflate the activity layout
        setContentView(binding.root) // Set the content view
        init() // Initialize the activity components
    }

    private fun init() {
        getProfileDataFromSharedPreferences() // Retrieve profile data from SharedPreferences
        firebaseUser = FirebaseAuth.getInstance().currentUser!! // Get the current Firebase user
        binding.editIcon.setOnClickListener(this) // Set click listener for the edit icon
    }

    private fun getProfileDataFromSharedPreferences() {
        val sdoData = getSharedPreferences("sdoData", MODE_PRIVATE)
        binding.name.text = sdoData.getString("name", "") // Set user name from SharedPreferences
        binding.email.text = sdoData.getString("email", "") // Set user email from SharedPreferences
        binding.city.text = sdoData.getString("city", "") // Set user city from SharedPreferences
        binding.subDivision.text = sdoData.getString("subDivision", "") // Set user subdivision from SharedPreferences
    }

    private fun createPasswordDialog() {
        userEditPasswordDialogBinding =
            UserEditPasswordDialogBinding.inflate(LayoutInflater.from(this)) // Inflate the password edit dialog layout
        userEditPasswordDialog = Dialog(this) // Create a new dialog
        userEditPasswordDialog.setContentView(userEditPasswordDialogBinding.root) // Set the dialog content view
        userEditPasswordDialog.setCancelable(false) // Make the dialog non-cancelable
        userEditPasswordDialog.show() // Show the dialog
        userEditPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set background color of the dialog window

        userEditPasswordDialogBinding.updateBtn.setOnClickListener {
            if (isValidPassword()) { // Check if the entered passwords are valid
                userEditPasswordDialogBinding.dialogProgressbar.visibility = VISIBLE
                verifyUserCurrentPassword(firebaseUser.email!!, userEditPasswordDialogBinding.userCurrentPassword.text.toString()) // Verify user's current password
            }
        }

        userEditPasswordDialogBinding.closeBtn.setOnClickListener{
            userEditPasswordDialog.dismiss() // Dismiss the password edit dialog
        }
    }

    private fun verifyUserCurrentPassword(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    updateUserPassword(userEditPasswordDialogBinding.userNewPassword.text.toString()) // Update user password if current password is verified
                }
            }.addOnFailureListener { e ->
                userEditPasswordDialogBinding.dialogProgressbar.visibility = GONE
                userEditPasswordDialogBinding.userCurrentPassword.error = "password is invalid" // Set error message for current password field
                Toast.makeText(this,"${e.message} --verifyUserCurrentPassword", Toast.LENGTH_SHORT).show() // Show error message
            }
    }

    private fun updateUserPassword(newPassword: String) {
        firebaseUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User Password Updated Successfully", Toast.LENGTH_SHORT).show() // Show success message
                    userEditPasswordDialogBinding.dialogProgressbar.visibility = GONE
                    userEditPasswordDialog.dismiss() // Dismiss the password edit dialog
                }
            }.addOnFailureListener { e ->
                userEditPasswordDialogBinding.dialogProgressbar.visibility = GONE
                Toast.makeText(this,"${e.message} --updateUserPassword", Toast.LENGTH_SHORT).show() // Show error message
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.editIcon -> {
                createPasswordDialog() // Open the password edit dialog on click of the edit icon
            }
        }
    }

    private fun isValidPassword(): Boolean {
        var valid = true

        if (userEditPasswordDialogBinding.userCurrentPassword.text!!.length < 6) {
            userEditPasswordDialogBinding.userCurrentPassword.error = "enter valid password" // Set error message for current password field
            valid = false
        }

        if (userEditPasswordDialogBinding.userNewPassword.text!!.length < 6) {
            userEditPasswordDialogBinding.userNewPassword.error = "enter valid password" // Set error message for new password field
            valid = false
        }

        return valid
    }
}