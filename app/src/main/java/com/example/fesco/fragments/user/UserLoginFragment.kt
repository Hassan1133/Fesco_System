package com.example.fesco.fragments.user

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
import com.example.fesco.activities.user.UserMainActivity
import com.example.fesco.activities.user.UserSignUpActivity
import com.example.fesco.databinding.FragmentUserLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserLoginBinding;

    private lateinit var usersRef: String

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var loadingDialog: Dialog

    private lateinit var user: UserModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.signUpTxt.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)

        firestoreDb = Firebase.firestore

        usersRef = "Users"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signUpTxt -> {
                val intent = Intent(activity, UserSignUpActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            R.id.loginBtn -> {

                val networkManager = NetworkManager(requireActivity())

                val isConnected = networkManager.isNetworkAvailable()

                if (isConnected) {
                    if (isDataValid()) {
                        signIn()
                    }
                } else {
                    Toast.makeText(
                        requireActivity(), "Please connect to internet", Toast.LENGTH_SHORT
                    ).show()
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

        loadingDialog = LoadingDialog.showLoadingDialog(activity)

        firestoreDb.collection(usersRef).document(binding.consumerNo.text.toString()).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    if (it.getString("consumerID") == binding.consumerNo.text.toString()) {
                        if (it.getString("key") == binding.password.text.toString()) {
                            user = it.toObject(UserModel::class.java)!!
                            goToUserMainActivity(user)
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

    private fun goToUserMainActivity(model: UserModel) {

        getUserProfileData(model)

        LoadingDialog.hideLoadingDialog(loadingDialog)

        val pref = activity?.getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("userFlag", true)
        editor?.apply()

        activity?.let {
            val intent = Intent(activity, UserMainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun getUserProfileData(model: UserModel) {
        val userData = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val editor = userData?.edit()

        editor?.putString("consumerID", model.consumerID)
        editor?.putString("name", model.name)
        editor?.putString("address", model.address)
        editor?.putString("phoneNo", model.phoneNo)
        editor?.putString("ls", model.ls)
        editor?.apply()
    }
}