package com.example.fesco.fragments.lm

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
import com.example.fesco.activities.lm.LMMainActivity
import com.example.fesco.databinding.FragmentLmLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.LMModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class LMLoginFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentLmLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmRef: String

    private lateinit var lmModel: LMModel

    private lateinit var loadingDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLmLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    // Initialize Firebase components
    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore

        // Set click listener for login button
        binding.loginBtn.setOnClickListener(this)
        lmRef = "LM"
    }

    // Handle click events
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
                            signIn(binding.email.text.toString(), binding.password.text.toString())
                        }
                    } else {
                        activity?.let {
                            Toast.makeText(
                                requireActivity(), "Please connect to internet", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // Handle network check exception
                    activity?.let {
                        Toast.makeText(
                            requireActivity(), "Network check failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // Sign in with email and password
    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Check if LM exists after successful sign-in
                loadingDialog = LoadingDialog.showLoadingDialog(activity)
                checkLMExists(task.result.user!!.uid)
            }
        }.addOnFailureListener { exception ->
            // Handle authentication failure
            LoadingDialog.hideLoadingDialog(loadingDialog)
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check if LM exists in Firestore
    private fun checkLMExists(userId: String) {
        firestoreDb.collection(lmRef).document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                // LM exists, retrieve data
                lmModel = document.toObject(LMModel::class.java)!!
                // Get FCM token
                getFCMToken(lmModel)
            } else {
                // LM doesn't exist
                LoadingDialog.hideLoadingDialog(loadingDialog)
                activity?.let {
                    Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { exception ->
            // Handle Firestore query failure
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Get FCM token for notifications
    private fun getFCMToken(lmModel: LMModel) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Update FCM token in Firestore
            setFCMTokenToDb(token, lmModel)
        }.addOnFailureListener { exception ->
            // Handle FCM token retrieval failure
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Update FCM token in Firestore
    private fun setFCMTokenToDb(token: String?, lmModel: LMModel) {
        firestoreDb.runTransaction { transaction ->
            val lmDocRef = firestoreDb.collection("LM").document(lmModel.id)
            transaction.update(lmDocRef, "lmFCMToken", token)

        }.addOnSuccessListener {
            // Update LMModel with FCM token
            lmModel.lmFCMToken = token!!
            // Proceed to main activity
            goToLMMainActivity(lmModel)
        }.addOnFailureListener { exception ->
            // Handle transaction failure
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validate user input
    private fun isDataValid(): Boolean {
        var valid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            binding.email.error = "Please enter valid email address"
            valid = false
        }
        if (binding.password.text.isNullOrEmpty() || binding.password.text!!.length < 6) {
            binding.password.error = "Please enter valid password"
            valid = false
        }
        return valid
    }

    // Proceed to LM main activity after successful login
    private fun goToLMMainActivity(model: LMModel) {
        // Store login state in SharedPreferences
        setProfileDataToSharedPreferences(model)

        val pref = activity?.getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("lmFlag", true)
        editor?.apply()

        // Start LMMainActivity
        activity?.let {
            // Show success message
            Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(it, LMMainActivity::class.java)
            it.startActivity(intent)
            it.finish()
        }
    }

    // Store LM profile data in SharedPreferences
    private fun setProfileDataToSharedPreferences(model: LMModel) {
        val lmData = context?.getSharedPreferences("lmData", Context.MODE_PRIVATE)
        val editor = lmData?.edit()
        editor?.putString("id", model.id)
        editor?.putString("name", model.name)
        editor?.putString("city", model.city)
        editor?.putString("email", model.email)
        editor?.putString("ls", model.ls)
        editor?.putString("subDivision", model.subDivision)
        editor?.apply()
    }
}