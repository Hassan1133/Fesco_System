package com.example.fesco.fragments.sdo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fesco.R
import com.example.fesco.activities.sdo.SDOMainActivity
import com.example.fesco.databinding.FragmentSdoLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.SDOModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson

class SDOLoginFragment : Fragment(), OnClickListener {

    // Late-initialized properties
    private lateinit var binding: FragmentSdoLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var sdoRef: String
    private lateinit var loadingDialog: Dialog
    private lateinit var sdoModel: SDOModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSdoLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore

        // Set OnClickListener for login button
        binding.loginBtn.setOnClickListener(this)

        // Collection reference for SDO documents
        sdoRef = "SDO"
    }

    // Sign in with Firebase Authentication
    private fun signIn(email: String, password: String) {

        loadingDialog = LoadingDialog.showLoadingDialog(activity)

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkSDOExists(task.result.user!!.uid)
            }
        }.addOnFailureListener { exception ->
            // Handle sign-in failure
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    // Check if the SDO exists in Firestore
    private fun checkSDOExists(userId: String) {
        firestoreDb.collection(sdoRef).document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Convert Firestore document to SDOModel
                sdoModel = document.toObject(SDOModel::class.java)!!
                // Get FCM token for SDO
                getFCMToken(sdoModel)
            } else {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Handle Firestore query failure
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    // Get FCM token for the SDO
    private fun getFCMToken(sdoModel: SDOModel) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                setFCMTokenToDb(token, sdoModel)
            }.addOnFailureListener { exception ->
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    // Update FCM token in Firestore
    private fun setFCMTokenToDb(token: String?, sdoModel: SDOModel) {
        firestoreDb.runTransaction { transaction ->
            val lmDocRef = firestoreDb.collection("SDO").document(sdoModel.id)
            transaction.update(lmDocRef, "sdoFCMToken", token)

        }.addOnSuccessListener {
            sdoModel.sdoFCMToken = token!!
            goToSDOMainActivity(sdoModel)
        }.addOnFailureListener { exception ->
            // Handle transaction failure
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validate input data
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

    // Handle button clicks
    override fun onClick(v: View) {
        when (v.id) {
            R.id.loginBtn -> {
                // Check network connectivity
                val networkManager = NetworkManager(requireActivity())
                try {
                    val isConnected = networkManager.isNetworkAvailable()
                    if (isConnected) {
                        // Validate input data and proceed with sign-in
                        if (isDataValid()) {
                            signIn(binding.email.text.toString(), binding.password.text.toString())
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

    // Navigate to SDO main activity after successful login
    private fun goToSDOMainActivity(model: SDOModel) {
        setProfileDataToSharedPreferences(model)

        val pref = activity?.getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("sdoFlag", true)
        editor?.apply()

        Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()

        activity?.let {
            val intent = Intent(activity, SDOMainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    // Store SDO profile data in SharedPreferences
    private fun setProfileDataToSharedPreferences(model: SDOModel) {
        val sdoData = context?.getSharedPreferences("sdoData", Context.MODE_PRIVATE)
        val editor = sdoData?.edit()
        editor?.putString("id", model.id)
        editor?.putString("name", model.name)
        editor?.putString("city", model.city)
        editor?.putString("xen", model.xen)
        editor?.putString("email", model.email)
        editor?.putString("subDivision", model.subDivision)
        // Convert the List<String> to a JSON string
        val lsJson = Gson().toJson(model.ls)
        editor?.putString("ls", lsJson)

        val areaJson = Gson().toJson(model.area)
        editor?.putString("area", areaJson)

        editor?.apply()
    }
}
