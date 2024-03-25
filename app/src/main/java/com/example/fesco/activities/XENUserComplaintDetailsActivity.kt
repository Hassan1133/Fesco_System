package com.example.fesco.activities

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
        loadingDialog = LoadingDialog.showLoadingDialog(this@XENUserComplaintDetailsActivity)!!
        firestoreDb = Firebase.firestore
        getDataFromIntentSetToFields()
    }

    private fun getDataFromIntentSetToFields() {
        userComplaintModel = intent.getSerializableExtra("userComplaintModel") as UserComplaintModel
        binding.name.text = userComplaintModel.userName
        binding.consumerID.text = userComplaintModel.consumerID
        binding.dateTime.text = userComplaintModel.dateTime
        binding.complaintType.text = userComplaintModel.complaintType
        binding.address.text = userComplaintModel.address
        binding.status.text = userComplaintModel.status
        binding.phone.text = userComplaintModel.phoneNo
        binding.feedback.text = userComplaintModel.feedback

        getSDOId(userComplaintModel.consumerID)
    }

    private fun getSDOId(consumerID: String) {
        firestoreDb.collection("Users").document(consumerID).get()
            .addOnSuccessListener {
                getSDOData(it.getString("sdo"))
            }
            .addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@XENUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getSDOData(lsId: String?) {
        firestoreDb.collection("SDO").document(lsId!!).get()
            .addOnSuccessListener {
                binding.sdo.text = it.getString("name")
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@XENUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }
}