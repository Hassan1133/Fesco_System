package com.example.fesco.activities.xen

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.databinding.ActivityXenuserComplaintDetailsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class XENUserComplaintDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityXenuserComplaintDetailsBinding
    private lateinit var userComplaintModel: UserComplaintModel
    private lateinit var loadingDialog: Dialog
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXenuserComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        // Initialize loading dialog
        loadingDialog = LoadingDialog.showLoadingDialog(this@XENUserComplaintDetailsActivity)!!

        // Initialize Firestore database
        firestoreDb = Firebase.firestore

        // Retrieve data from intent and set to UI fields
        getDataFromIntentSetToFields()
    }

    private fun getDataFromIntentSetToFields() {
        // Retrieve UserComplaintModel object from intent
        userComplaintModel = intent.getSerializableExtra("userComplaintModel") as UserComplaintModel

        // Set data from UserComplaintModel to UI fields
        binding.name.text = userComplaintModel.userName
        binding.consumerID.text = userComplaintModel.consumerID
        binding.dateTime.text = userComplaintModel.dateTime
        binding.complaintType.text = userComplaintModel.complaintType
        binding.address.text = userComplaintModel.address
        binding.status.text = userComplaintModel.status
        binding.phone.text = userComplaintModel.phoneNo
        binding.feedback.text = userComplaintModel.feedback

        // Retrieve SDO ID associated with the complaint
        getSDOId(userComplaintModel.consumerID)
    }

    private fun getSDOId(consumerID: String) {
        // Retrieve SDO ID from Firestore based on consumer ID
        firestoreDb.collection("Users").document(consumerID).get()
            .addOnSuccessListener { document ->
                // Get the LS ID associated with the consumer
                val lsId = document.getString("ls")
                if (lsId != null) {
                    // Retrieve SDO data based on LS ID
                    getLsData(lsId)
                } else {
                    // Handle the case when LS ID is null
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(this@XENUserComplaintDetailsActivity, "LS ID is null", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle Firestore query failure
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@XENUserComplaintDetailsActivity, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLsData(lsId: String) {
        // Retrieve SDO ID from Firestore based on LS ID
        firestoreDb.collection("LS").document(lsId).get()
            .addOnSuccessListener { document ->
                // Get the SDO ID associated with the LS
                val sdoId = document.getString("sdo")
                if (sdoId != null) {
                    // Retrieve SDO data based on SDO ID
                    getSDOData(sdoId)
                } else {
                    // Handle the case when SDO ID is null
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(this@XENUserComplaintDetailsActivity, "SDO ID is null", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle Firestore query failure
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@XENUserComplaintDetailsActivity, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getSDOData(sdoId: String) {
        // Retrieve SDO data from Firestore based on SDO ID
        firestoreDb.collection("SDO").document(sdoId).get()
            .addOnSuccessListener { document ->
                // Set SDO name to UI field
                binding.sdo.text = document.getString("name")
                // Hide loading dialog after data retrieval
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
            .addOnFailureListener { exception ->
                // Handle Firestore query failure
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@XENUserComplaintDetailsActivity, exception.message, Toast.LENGTH_SHORT).show()
            }
    }
}