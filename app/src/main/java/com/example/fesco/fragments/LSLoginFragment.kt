package com.example.fesco.fragments

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
import com.example.fesco.activities.LSMainActivity
import com.example.fesco.databinding.FragmentLsLoginBinding
import com.example.fesco.main_utils.LoadingDialog
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
    ): View? {
        binding = FragmentLsLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore

        binding.loginBtn.setOnClickListener(this)
        lsRef = "LS"
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                checkLSExists(it.result.user!!.uid)
            }

        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLSExists(userId: String) {
        firestoreDb.collection(lsRef).document(userId).get().addOnSuccessListener {

            if (it.exists()) {
                lsModel = it.toObject(LSModel::class.java)!!
                getFCMToken(lsModel)
                Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()
            } else {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFCMToken(lsModel: LSModel) {
        FirebaseMessaging.getInstance().getToken()
            .addOnSuccessListener {
                setFCMTokenToDb(it, lsModel)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setFCMTokenToDb(token: String?, lsModel: LSModel) {
        firestoreDb.collection("LS").document(lsModel.id).update("lsFCMToken", token)
            .addOnSuccessListener {
                lsModel.lsFCMToken = token!!
                goToLSMainActivity(lsModel)
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

    override fun onClick(v: View) {
        when (v.id) {

            R.id.loginBtn -> {
                if (isDataValid()) {
                    loadingDialog = LoadingDialog.showLoadingDialog(activity)!!
                    signIn(binding.email.text.toString(), binding.password.text.toString())
                }
            }

        }
    }

    private fun goToLSMainActivity(model : LSModel) {

        setProfileDataToSharedPreferences(model)

        val pref = activity?.getSharedPreferences("login", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("lsFlag", true)
        editor?.apply()


        val intent = Intent(activity, LSMainActivity()::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun setProfileDataToSharedPreferences(model : LSModel) {
        val lsData = context?.getSharedPreferences("lsData", Context.MODE_PRIVATE)
        val editor = lsData?.edit()
        if (model != null) {
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
}