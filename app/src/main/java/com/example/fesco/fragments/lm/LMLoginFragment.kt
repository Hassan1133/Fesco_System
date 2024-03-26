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
    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore

        binding.loginBtn.setOnClickListener(this)
        lmRef = "LM"
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                checkLMExists(it.result.user!!.uid)
            }

        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLMExists(userId: String) {
        firestoreDb.collection(lmRef).document(userId).get().addOnSuccessListener {

            if (it.exists()) {
                lmModel = it.toObject(LMModel::class.java)!!
                getFCMToken(lmModel)
            } else {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFCMToken(lmModel: LMModel) {
        FirebaseMessaging.getInstance().getToken()
            .addOnSuccessListener {
                setFCMTokenToDb(it, lmModel)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setFCMTokenToDb(token: String?, lmModel: LMModel) {
        firestoreDb.collection("LM").document(lmModel.id).update("lmFCMToken", token)
            .addOnSuccessListener {
                lmModel.lmFCMToken = token!!
                goToLMMainActivity(lmModel)
            }.addOnFailureListener{
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }
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

    private fun goToLMMainActivity(model : LMModel) {

        setProfileDataToSharedPreferences(model)

        val pref = activity?.getSharedPreferences("fescoLogin", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("lmFlag", true)
        editor?.apply()

        Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(activity, LMMainActivity()::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun setProfileDataToSharedPreferences(model : LMModel) {
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