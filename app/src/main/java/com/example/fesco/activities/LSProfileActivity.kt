package com.example.fesco.activities

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
import com.example.fesco.databinding.ActivitySdoprofileBinding
import com.example.fesco.databinding.UserEditPasswordDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LSProfileActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityLsprofileBinding

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var userEditPasswordDialogBinding: UserEditPasswordDialogBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var userEditPasswordDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLsprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        getProfileDataFromSharedPreferences()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        binding.editIcon.setOnClickListener(this)
    }

    private fun getProfileDataFromSharedPreferences() {
        val lsData = getSharedPreferences("lsData", MODE_PRIVATE)
        binding.name.text = lsData.getString("name", "")
        binding.email.text = lsData.getString("email", "")
        binding.city.text = lsData.getString("city", "")
        binding.subDivision.text = lsData.getString("subDivision", "")
    }

    private fun createPasswordDialog() {
        userEditPasswordDialogBinding =
            UserEditPasswordDialogBinding.inflate(LayoutInflater.from(this))
        userEditPasswordDialog = Dialog(this)
        userEditPasswordDialog.setContentView(userEditPasswordDialogBinding.root)
        userEditPasswordDialog.setCancelable(false)
        userEditPasswordDialog.show()
        userEditPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        userEditPasswordDialogBinding.updateBtn.setOnClickListener {
            if (isValidPassword()) {
                loadingDialog = LoadingDialog.showLoadingDialog(this)!!
                verifyUserCurrentPassword(firebaseUser.email!!, userEditPasswordDialogBinding.userCurrentPassword.text.toString())
            }
        }

        userEditPasswordDialogBinding.closeBtn.setOnClickListener{
            userEditPasswordDialog.dismiss()
        }
    }

    private fun verifyUserCurrentPassword(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    updateUserPassword(userEditPasswordDialogBinding.userNewPassword.text.toString())
                }
            }.addOnFailureListener { e ->
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userEditPasswordDialogBinding.userCurrentPassword.error = "password is invalid"
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserPassword(newPassword: String) {
        firebaseUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User Password Updated Successfully", Toast.LENGTH_SHORT).show()
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    userEditPasswordDialog.dismiss()
                }
            }.addOnFailureListener { e ->
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.editIcon -> {
                createPasswordDialog()
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