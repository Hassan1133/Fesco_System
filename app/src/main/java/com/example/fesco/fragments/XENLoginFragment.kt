package com.example.fesco.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.fesco.R
import com.example.fesco.activities.UserMainActivity
import com.example.fesco.activities.XENMainActivity
import com.example.fesco.databinding.FragmentXenLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class XENLoginFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentXenLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var xenRef: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                if (isDataValid()) {
                    signIn(binding.email.text.toString(), binding.password.text.toString())
                }
            }

        }
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                checkXENExists(it.result.user!!.uid)
            }

        }.addOnFailureListener {
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkXENExists(userId: String) {
        firestoreDb.collection(xenRef).document(userId).get().addOnSuccessListener {

            if (it.exists()) {
                Toast.makeText(activity, "Logged In Successfully", Toast.LENGTH_SHORT).show()
                goToXENMainActivity()
            }
            else
            {
                Toast.makeText(activity, "Account doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
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

    private fun goToXENMainActivity() {
        val intent: Intent = Intent(activity, XENMainActivity()::class.java)
        startActivity(intent)
        activity?.finish()
    }
}

