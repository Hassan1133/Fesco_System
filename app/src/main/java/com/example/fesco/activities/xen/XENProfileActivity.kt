package com.example.fesco.activities.xen

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.databinding.ActivityXenprofileBinding
import com.example.fesco.databinding.UserEditPasswordDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class XENProfileActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityXenprofileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var userEditPasswordDialogBinding: UserEditPasswordDialogBinding
    private lateinit var userEditPasswordDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXenprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        init()
    }
    private fun init() {
        // Initialize UI components and Firebase user
        getProfileDataFromSharedPreferences()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        binding.editIcon.setOnClickListener(this)
    }

    private fun getProfileDataFromSharedPreferences() {
        // Retrieve profile data from SharedPreferences and populate UI fields
        val xenData = getSharedPreferences("xenData", MODE_PRIVATE)
        binding.name.text = xenData.getString("name", "")
        binding.email.text = xenData.getString("email", "")
        binding.city.text = xenData.getString("city", "")
        binding.division.text = xenData.getString("division", "")
    }

    private fun createPasswordDialog() {
        // Create and show the dialog for editing password
        userEditPasswordDialogBinding = UserEditPasswordDialogBinding.inflate(LayoutInflater.from(this))
        userEditPasswordDialog = Dialog(this)
        userEditPasswordDialog.setContentView(userEditPasswordDialogBinding.root)
        userEditPasswordDialog.setCancelable(false)
        userEditPasswordDialog.show()
        userEditPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        userEditPasswordDialogBinding.updateBtn.setOnClickListener {
            // When the user clicks on the update button
            if (isValidPassword()) {
                userEditPasswordDialogBinding.dialogProgressbar.visibility = View.VISIBLE
                verifyUserCurrentPassword(firebaseUser.email!!, userEditPasswordDialogBinding.userCurrentPassword.text.toString())
            }
        }

        userEditPasswordDialogBinding.closeBtn.setOnClickListener {
            // When the user clicks on the close button
            userEditPasswordDialog.dismiss()
        }
    }

    private fun verifyUserCurrentPassword(email: String, password: String) {
        // Verify the user's current password before allowing password update
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    updateUserPassword(userEditPasswordDialogBinding.userNewPassword.text.toString())
                }
            }.addOnFailureListener { e ->
                userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                userEditPasswordDialogBinding.userCurrentPassword.error = "Password is invalid"
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserPassword(newPassword: String) {
        // Update the user's password in Firebase Authentication
        firebaseUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User Password Updated Successfully", Toast.LENGTH_SHORT).show()
                    userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                    userEditPasswordDialog.dismiss()
                }
            }.addOnFailureListener { e ->
                userEditPasswordDialogBinding.dialogProgressbar.visibility = View.GONE
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View) {
        // Handle click events
        when (v.id) {
            R.id.editIcon -> {
                createPasswordDialog() // Show password edit dialog when edit icon is clicked
            }
        }
    }

    private fun isValidPassword(): Boolean {
        // Validate the new password entered by the user
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
