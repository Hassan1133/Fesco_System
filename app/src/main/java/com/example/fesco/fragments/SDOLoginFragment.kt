package com.example.fesco.fragments

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
import com.example.fesco.activities.SDOMainActivity
import com.example.fesco.databinding.FragmentSdoLoginBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.SDOModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class SDOLoginFragment : Fragment(), OnClickListener {

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
    ): View? {
        binding = FragmentSdoLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore

        binding.loginBtn.setOnClickListener(this)
        sdoRef = "SDO"
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                checkSDOExists(it.result.user!!.uid)
            }

        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkSDOExists(userId: String) {
        firestoreDb.collection(sdoRef).document(userId).get().addOnSuccessListener {

            if (it.exists()) {
                sdoModel = it.toObject(SDOModel::class.java)!!
                goToSDOMainActivity(sdoModel)
                Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()
            }
            else
            {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
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

    private fun goToSDOMainActivity(model : SDOModel) {

        setProfileDataToSharedPreferences(model)

        val pref = activity?.getSharedPreferences("login", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putBoolean("sdoFlag", true)
        editor?.apply()


        val intent = Intent(activity, SDOMainActivity()::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun setProfileDataToSharedPreferences(model : SDOModel) {
        val sdoData = context?.getSharedPreferences("sdoData", Context.MODE_PRIVATE)
        val editor = sdoData?.edit()
        if (model != null) {
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
}