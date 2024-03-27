package com.example.fesco.activities.lm

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.databinding.ActivityLmuserComplaintDetailsBinding
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class LMUserComplaintDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLmuserComplaintDetailsBinding
    private lateinit var userComplaintModel: UserComplaintModel
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout
        binding = ActivityLmuserComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        // Initialize Firestore and populate fields
        init()
    }

    private fun init() {
        // Initialize Firestore
        firestore = Firebase.firestore
        // Retrieve data from intent and set it to UI fields
        getDataFromIntentSetToFields()
    }

    private fun getDataFromIntentSetToFields() {
        // Retrieve user complaint data from intent
        userComplaintModel = intent.getSerializableExtra("userComplaintModel") as UserComplaintModel
        // Set user complaint data to respective UI elements
        binding.name.text = userComplaintModel.userName
        binding.consumerID.text = userComplaintModel.consumerID
        binding.dateTime.text = userComplaintModel.dateTime
        binding.complaintType.text = userComplaintModel.complaintType
        binding.address.text = userComplaintModel.address
        binding.phone.text = userComplaintModel.phoneNo
        binding.feedback.text = userComplaintModel.feedback

        // Set up AutoCompleteTextView for complaint status
        setupAutoCompleteTextView()
    }

    private fun setupAutoCompleteTextView() {
        // Retrieve status options from resources
        val statusArray = resources.getStringArray(R.array.status_array)
        // Create ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusArray)
        // Set adapter to AutoCompleteTextView
        binding.status.setAdapter(adapter)

        // Set initial status value and handle item click
        binding.status.setText(userComplaintModel.status, false)
        // Set item click listener
        binding.status.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            // Update complaint status based on selected item
            if (selectedItem == "In Process") {
                updatedComplaintStatusToDb("In Process")
            } else if (selectedItem == "Resolved") {
                updatedComplaintStatusToDb("Resolved")
            }
        }
    }

    private fun updatedComplaintStatusToDb(status: String) {
        // Update complaint status in Firestore
        firestore.collection("UserComplaints").document(userComplaintModel.id)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this@LMUserComplaintDetailsActivity, "Complaint status updated successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this@LMUserComplaintDetailsActivity, "${it.message} --updatedComplaintStatusToDb", Toast.LENGTH_SHORT).show()
            }
    }
}