package com.example.fesco.fragments.xen

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
import com.example.fesco.activities.xen.XENMainActivity
import com.example.fesco.databinding.FragmentXenLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.XENModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson

class XENLoginFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentXenLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var xenRef: String

    private lateinit var xenModel: XENModel

    private lateinit var loadingDialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXenLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore

        binding.loginBtn.setOnClickListener(this)
        xenRef = "XEN"
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.loginBtn -> {

                val networkManager = NetworkManager(requireActivity())

                val isConnected = networkManager.isNetworkAvailable()

                if (isConnected) {
                    if (isDataValid()) {
                        signIn(binding.email.text.toString(), binding.password.text.toString())
                    }
                } else {
                    Toast.makeText(
                        requireActivity(), "Please connect to internet", Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                loadingDialog = LoadingDialog.showLoadingDialog(activity)
                checkXENExists(it.result.user!!.uid)
            }

        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkXENExists(userId: String) {
        firestoreDb.collection(xenRef).document(userId).get().addOnSuccessListener {

            if (it.exists()) {
                xenModel = it.toObject(XENModel::class.java)!!
                getFCMToken(xenModel)
            } else {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFCMToken(xenModel: XENModel) {
        FirebaseMessaging.getInstance().getToken()
            .addOnSuccessListener {
                setFCMTokenToDb(it, xenModel)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setFCMTokenToDb(token: String?, xenModel: XENModel) {
        firestoreDb.collection("XEN").document(xenModel.id).update("xenFCMToken", token)
            .addOnSuccessListener {
                xenModel.xenFCMToken = token!!
                goToXENMainActivity(xenModel)
            }.addOnFailureListener{
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }
    private fun isDataValid(): Boolean {
        var valid: Boolean = true
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

    private fun goToXENMainActivity(model : XENModel) {

        setProfileData(model)

        val pref = activity?.getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("xenFlag", true)
        editor?.apply()

        Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()

        activity?.let {
            val intent = Intent(activity, XENMainActivity()::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun setProfileData(model: XENModel) {
        val xenData = context?.getSharedPreferences("xenData", Context.MODE_PRIVATE)
        val editor = xenData?.edit()
        editor?.putString("id", model.id)
        editor?.putString("name", model.name)
        editor?.putString("city", model.city)
        editor?.putString("email", model.email)
        editor?.putString("division", model.division)
        // Convert the List<String> to a JSON string
        val sdoJson = Gson().toJson(model.sdo)
        editor?.putString("sdo", sdoJson)
        editor?.apply()
    }
}

