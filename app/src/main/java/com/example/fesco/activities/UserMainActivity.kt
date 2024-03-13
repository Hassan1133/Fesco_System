package com.example.fesco.activities

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserMainBinding
import com.example.fesco.databinding.ComplaintDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class UserMainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserMainBinding

    private lateinit var userComplaintDialogBinding: ComplaintDialogBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var userComplaintDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var userData: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        binding.addComplaintBtn.setOnClickListener(this)
        firestoreDb = Firebase.firestore
        setUserName()
    }

    private fun setUserName() {
        userData = getSharedPreferences("userData", MODE_PRIVATE)
        binding.name.text = userData.getString("name", "")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> showLogoutDialog()

            R.id.profile -> startActivity(Intent(this, UserProfileActivity::class.java))

            R.id.addComplaintBtn -> createComplaintDialog()
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this).setMessage(R.string.logout_message).setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> logOut() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun createComplaintDialog() {
        userComplaintDialogBinding = ComplaintDialogBinding.inflate(LayoutInflater.from(this))
        userComplaintDialog = Dialog(this)
        userComplaintDialog.setContentView(userComplaintDialogBinding.root)
        userComplaintDialog.setCancelable(false)
        userComplaintDialog.show()
        userComplaintDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            getComplaintTypesFromDB()?.let { complaintTypes ->
                userComplaintDialogBinding.complaintType.setAdapter(
                    ArrayAdapter(
                        this@UserMainActivity, android.R.layout.simple_list_item_1, complaintTypes
                    )
                )
            }
        }

        userComplaintDialogBinding.closeBtn.setOnClickListener {
            userComplaintDialog.dismiss()
        }

        userComplaintDialogBinding.submitBtn.setOnClickListener {
            if (isValid()) {
                loadingDialog = LoadingDialog.showLoadingDialog(this)!!
                setComplaintDataToModel()
            }
        }
    }

    private fun setComplaintDataToModel() {
        val model = UserComplaintModel()
        model.consumerID = userData.getString("consumerID", "")!!
        model.userName = userData.getString("name", "")!!
        model.address = userData.getString("address", "")!!
        model.phoneNo = userData.getString("phoneNo", "")!!
        model.complaintType = userComplaintDialogBinding.complaintType.text.toString()
        model.dateTime = getCurrentDateTime()!!

        submitComplaint(model)
    }

    private fun isValid(): Boolean {
        var valid = true
        if (userComplaintDialogBinding.complaintType.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please select complaint type", Toast.LENGTH_SHORT).show()
            valid = false
        }
        return valid
    }

    private fun submitComplaint(model: UserComplaintModel) {
        val dbDocument = firestoreDb.collection("UserComplaints").document()
        model.id = dbDocument.id
        dbDocument.set(model).addOnSuccessListener {
            retrieveCurrentList(model.id)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveCurrentList(id: String) {
        firestoreDb.collection("LS").document(userData.getString("ls", "")!!)
            .get()
            .addOnSuccessListener { snapShot ->
                val currentComplaints = snapShot.get("complaints") as? List<String> ?: emptyList()
                val updatedComplaints = currentComplaints.filter { it.isNotEmpty() }.toMutableList()
                updatedComplaints.add(id)
                sendComplaintIDToLS(id, updatedComplaints)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendComplaintIDToLS(id: String, list : List<String>) {
        firestoreDb.collection("LS").document(userData.getString("ls", "")!!)
            .update("complaints", list)
            .addOnSuccessListener {
                Toast.makeText(this, "Complaint Submitted Successfully", Toast.LENGTH_SHORT).show()
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userComplaintDialog.dismiss()
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentDateTime(): String? {
        val dateFormat = SimpleDateFormat("d MMM yyyy hh:mm a")
        return dateFormat.format(Date())
    }

    private suspend fun getComplaintTypesFromDB(): List<String>? {
        return try {
            firestoreDb.collection("ComplaintTypes").document("allComplaintTypes").get().await()
                .get("types") as List<String>
        } catch (e: Exception) {
            null
        }
    }

    private fun logOut() {

        val userData = getSharedPreferences("userData", MODE_PRIVATE)
        val profileDataEditor = userData.edit()
        profileDataEditor.clear()
        profileDataEditor.apply()

        val pref = getSharedPreferences("login", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("userFlag", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}