package com.example.fesco.activities.sdo

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

        // Initialize Firestore database and load data from intent
        init()
    }

    private fun init() {
        // Show loading dialog while fetching data
        loadingDialog = LoadingDialog.showLoadingDialog(this@SDOUserComplaintDetailsActivity)!!

        // Initialize Firestore database
        firestoreDb = Firebase.firestore

        // Fetch data from intent and set it to UI fields
        getDataFromIntentSetToFields()
    }

    private fun getDataFromIntentSetToFields() {
        // Get user complaint model from intent
        userComplaintModel = intent.getSerializableExtra("userComplaintModel") as UserComplaintModel

        // Set complaint details to UI fields
        binding.name.text = userComplaintModel.userName
        binding.consumerID.text = userComplaintModel.consumerID
        binding.dateTime.text = userComplaintModel.dateTime
        binding.complaintType.text = userComplaintModel.complaintType
        binding.address.text = userComplaintModel.address
        binding.status.text = userComplaintModel.status
        binding.phone.text = userComplaintModel.phoneNo
        binding.feedback.text = userComplaintModel.feedback

        // Fetch LS ID from Firestore based on consumer ID
        getLsId(userComplaintModel.consumerID)
    }

    private fun getLsId(consumerID: String) {
        // Fetch LS ID from Firestore based on consumer ID
        firestoreDb.collection("Users").document(consumerID).get()
            .addOnSuccessListener {
                // Once LS ID is fetched, get LS data
                getLsData(it.getString("ls"))
            }
            .addOnFailureListener {
                // Handle failure to fetch LS ID
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@SDOUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLsData(lsId: String?) {
        // Fetch LS data from Firestore based on LS ID
        firestoreDb.collection("LS").document(lsId!!).get()
            .addOnSuccessListener {
                // Set LS name to UI field
                binding.ls.text = it.getString("name")
                // Hide loading dialog after LS data is fetched and set
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }.addOnFailureListener {
                // Handle failure to fetch LS data
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@SDOUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}
