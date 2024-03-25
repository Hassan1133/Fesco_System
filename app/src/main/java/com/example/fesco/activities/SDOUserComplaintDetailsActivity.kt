package com.example.fesco.activities

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.databinding.ActivitySdouserComplaintDetailsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class SDOUserComplaintDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySdouserComplaintDetailsBinding

    private lateinit var userComplaintModel: UserComplaintModel

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdouserComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        loadingDialog = LoadingDialog.showLoadingDialog(this@SDOUserComplaintDetailsActivity)!!
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

        getLsId(userComplaintModel.consumerID)
    }

    private fun getLsId(consumerID: String) {
        firestoreDb.collection("Users").document(consumerID).get()
            .addOnSuccessListener {
                getLsData( it.getString("ls"))
            }
            .addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@SDOUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLsData(lsId: String?){
        firestoreDb.collection("LS").document(lsId!!).get()
            .addOnSuccessListener {
                binding.ls.text = it.getString("name")
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }.addOnFailureListener{
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@SDOUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}