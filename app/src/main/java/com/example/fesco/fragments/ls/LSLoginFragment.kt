package com.example.fesco.fragments.ls

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.activities.ls.LSMainActivity
import com.example.fesco.databinding.FragmentLsLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.LSModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson

class LSLoginFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentLsLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lsRef: String
    private lateinit var lsModel: LSModel
    private lateinit var loadingDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLsLoginBinding.inflate(inflater, container, false)
        // Initialize fragment components
        init()
        return binding.root
    }

    private fun init() {
        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore
        // Set click listener for login button
        binding.loginBtn.setOnClickListener(this)
        // Reference for Firestore collection
        lsRef = "LS"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.loginBtn -> {
                // Check network connectivity
                val networkManager = NetworkManager(requireActivity())
                try {
                    val isConnected = networkManager.isNetworkAvailable()
                    if (isConnected) {
                        // Validate user input
                        if (isDataValid()) {
                            // Perform sign in
                            signIn(
                                binding.email.text.toString(),
                                binding.password.text.toString()
                            )
                        }
                    } else {
                        Toast.makeText(
                            requireActivity(), "Please connect to the internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    // Handle network check exception
                    Toast.makeText(
                        requireActivity(), "Network check failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadingDialog = LoadingDialog.showLoadingDialog(activity)
                    checkLSExists(task.result.user!!.uid)
                } else {
                    // Hide loading dialog and display error message
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(activity, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkLSExists(userId: String) {
        firestoreDb.collection(lsRef).document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // LS account exists, retrieve LS data
                    lsModel = document.toObject(LSModel::class.java)!!
                    getFCMToken(lsModel)
                } else {
                    // Hide loading dialog and display error message
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Hide loading dialog and display error message
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFCMToken(lsModel: LSModel) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                setFCMTokenToDb(token, lsModel)
            }
            .addOnFailureListener { exception ->
                // Hide loading dialog and display error message
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setFCMTokenToDb(token: String?, lsModel: LSModel) {
        firestoreDb.runTransaction { transaction ->
            val lmDocRef = firestoreDb.collection("LS").document(lsModel.id)
            transaction.update(lmDocRef, "lsFCMToken", token)

        }.addOnSuccessListener {
            // Update FCM token in Firestore and proceed to LSMainActivity
            lsModel.lsFCMToken = token!!
            goToLSMainActivity(lsModel)
        }.addOnFailureListener { exception ->
            // Handle transaction failure
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToLSMainActivity(model: LSModel) {
        // Save LS data to SharedPreferences
        setProfileDataToSharedPreferences(model)
        // Save LS flag to indicate successful login
        val pref = activity?.getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("lsFlag", true)
        editor?.apply()
        // Display success message and navigate to LSMainActivity
        Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, LSMainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun isDataValid(): Boolean {
        // Validate email and password fields
        var valid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            binding.email.error = "Please enter a valid email address"
            valid = false
        }
        if (binding.password.text.isNullOrEmpty() || binding.password.text!!.length < 6) {
            binding.password.error = "Please enter a valid password (minimum 6 characters)"
            valid = false
        }
        return valid
    }

    private fun setProfileDataToSharedPreferences(model: LSModel) {
        // Save LS data to SharedPreferences
        val lsData = context?.getSharedPreferences("lsData", Context.MODE_PRIVATE)
        val editor = lsData?.edit()
        editor?.putString("id", model.id)
        editor?.putString("name", model.name)
        editor?.putString("city", model.city)
        editor?.putString("email", model.email)
        editor?.putString("subDivision", model.subDivision)
        editor?.putString("sdo", model.sdo)
        val lmJson = Gson().toJson(model.lm)
        editor?.putString("lm", lmJson)
        editor?.apply()
    }
}
