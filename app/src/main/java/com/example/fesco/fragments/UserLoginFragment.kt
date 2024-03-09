package com.example.fesco.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.activities.UserMainActivity
import com.example.fesco.activities.UserSignUpActivity
import com.example.fesco.databinding.FragmentUserLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserLoginBinding;

    private lateinit var usersRef: String

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var loadingDialog: Dialog

    private lateinit var user: User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.signUpTxt.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)

        firestoreDb = Firebase.firestore

        usersRef = "Users"

        user = User()
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
                    loadingDialog = LoadingDialog.showLoadingDialog(activity)!!
                    signIn()
                }
            }
        }
    }

    private fun isDataValid(): Boolean {
        var valid = true
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
        firestoreDb.collection(usersRef).document(binding.consumerNo.text.toString()).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    if (it.getString("consumerID") == binding.consumerNo.text.toString()) {
                        if (it.getString("key") == binding.password.text.toString()) {
                            user.consumerID = it.getString("consumerID")!!
                            user.name = it.getString("name")!!
                            user.phoneNo = it.getString("phoneNo")!!
                            user.address = it.getString("address")!!
                            goToUserMainActivity()
                            Toast.makeText(activity, "Logged in successfully", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            LoadingDialog.hideLoadingDialog(loadingDialog)
                            Toast.makeText(activity, "Incorrect password", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                        Toast.makeText(activity, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(activity, "Invalid consumer ID", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToUserMainActivity() {

        getUserProfileData()

        LoadingDialog.hideLoadingDialog(loadingDialog)

        val pref = activity?.getSharedPreferences("login", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("userFlag", true)
        editor?.apply()

        val intent = Intent(activity, UserMainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun getUserProfileData() {
        val userData = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val editor = userData?.edit()

        if (user != null) {
            editor?.putString("consumerID", user.consumerID)
            editor?.putString("name", user.name)
            editor?.putString("address", user.address)
            editor?.putString("phoneNo", user.phoneNo)
            editor?.apply()
        }
    }
}