package com.example.fesco.activities

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserProfileBinding
import com.example.fesco.databinding.UserEditPasswordDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserProfileActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserProfileBinding

    private lateinit var userEditPasswordDialogBinding: UserEditPasswordDialogBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var userEditPasswordDialog: Dialog

    private lateinit var usersRef: String

    private lateinit var userData: SharedPreferences

    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        getProfileDataFromSharedPreferences()
        binding.editIcon.setOnClickListener(this)
        firestoreDb = Firebase.firestore
        usersRef = "Users"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.editIcon -> {
                createPasswordDialog()
            }
        }
    }

    private fun getProfileDataFromSharedPreferences() {
        userData = getSharedPreferences("userData", MODE_PRIVATE)
        binding.name.text = userData.getString("name", "")
        binding.consumerID.text = userData.getString("consumerID", "")
        binding.phone.text = userData.getString("phoneNo", "")
        binding.address.text = userData.getString("address", "")
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
                verifyUserCurrentPassword(
                    userData.getString("consumerID", "")!!,
                    userEditPasswordDialogBinding.userCurrentPassword.text.toString(),
                    userEditPasswordDialogBinding.userNewPassword.text.toString()
                )
            }
        }

        userEditPasswordDialogBinding.closeBtn.setOnClickListener {
            userEditPasswordDialog.dismiss()
        }
    }

    private fun verifyUserCurrentPassword(id: String, currentKey: String, newKey: String) {
        firestoreDb.collection(usersRef).document(id).get().addOnSuccessListener {
            if (it.getString("key") == currentKey) {
                updateUserKey(id, newKey)
            } else {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userEditPasswordDialogBinding.userCurrentPassword.error =
                    "Incorrect current password"
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserKey(id: String, newKey: String) {
        firestoreDb.collection(usersRef).document(id).update("key", newKey)
            .addOnSuccessListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userEditPasswordDialog.dismiss()
                Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
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