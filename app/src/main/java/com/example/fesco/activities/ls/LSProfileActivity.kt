package com.example.fesco.activities.ls

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
import com.example.fesco.databinding.ActivityLsprofileBinding
import com.example.fesco.databinding.UserEditPasswordDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LSProfileActivity : AppCompatActivity(), OnClickListener {

    // Late-initialized properties
    private lateinit var binding: ActivityLsprofileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var userEditPasswordDialogBinding: UserEditPasswordDialogBinding
    private lateinit var userEditPasswordDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLsprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init() // Initialize the activity components
    }

    private fun init() {
        getProfileDataFromSharedPreferences() // Get profile data from shared preferences
        firebaseUser = FirebaseAuth.getInstance().currentUser ?: return // Get current user
        binding.editIcon.setOnClickListener(this) // Set click listener for edit icon
    }

    private fun getProfileDataFromSharedPreferences() {
        val lsData = getSharedPreferences("lsData", MODE_PRIVATE)
        // Set user details from SharedPreferences
        binding.name.text = lsData.getString("name", "")
        binding.email.text = lsData.getString("email", "")
        binding.city.text = lsData.getString("city", "")
        binding.subDivision.text = lsData.getString("subDivision", "")
    }

    private fun createPasswordDialog() {
        userEditPasswordDialogBinding = UserEditPasswordDialogBinding.inflate(LayoutInflater.from(this))
        userEditPasswordDialog = Dialog(this)
        userEditPasswordDialog.setContentView(userEditPasswordDialogBinding.root)
        userEditPasswordDialog.setCancelable(false)
        userEditPasswordDialog.show()
        userEditPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        userEditPasswordDialogBinding.updateBtn.setOnClickListener { // Set click listener for update button
            if (isValidPassword()) {
                userEditPasswordDialogBinding.dialogProgressbar.visibility = View.VISIBLE
                verifyUserCurrentPassword(firebaseUser.email!!, userEditPasswordDialogBinding.userCurrentPassword.text.toString())
            }
        }

        userEditPasswordDialogBinding.closeBtn.setOnClickListener { // Set click listener for close button
            userEditPasswordDialog.dismiss() // Dismiss password dialog
        }
    }

    private fun verifyUserCurrentPassword(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUserPassword(userEditPasswordDialogBinding.userNewPassword.text.toString())
                } else {
                    // Hide loading dialog on failure
                    userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                    userEditPasswordDialogBinding.userCurrentPassword.error = "Invalid password"
                    // Show error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e -> // Handle general exceptions
                userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                userEditPasswordDialogBinding.userCurrentPassword.error = "Invalid password"
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserPassword(newPassword: String) {
        firebaseUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show()
                    // Hide loading dialog on success
                    userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                    // Dismiss password dialog
                    userEditPasswordDialog.dismiss()
                } else {
                    // Hide loading dialog on failure
                    userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                    // Show error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e -> // Handle general exceptions
                userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.editIcon -> {
                createPasswordDialog() // Open password edit dialog on click
            }
        }
    }

    private fun isValidPassword(): Boolean {
        var valid = true

        if (userEditPasswordDialogBinding.userCurrentPassword.text!!.length < 6) {
            userEditPasswordDialogBinding.userCurrentPassword.error = "enter valid password"
            valid = false
        }

        if (userEditPasswordDialogBinding.userNewPassword.text!!.length < 6) {
            userEditPasswordDialogBinding.userNewPassword.error = "enter valid password"
            valid = false
        }

        return valid
    }
}