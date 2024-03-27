package com.example.fesco.activities.user

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.R
import com.example.fesco.databinding.ActivityUserComplaintDetailsBinding
import com.example.fesco.databinding.ComplaintFeedbackDialogBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserComplaintDetailsActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityUserComplaintDetailsBinding
    private lateinit var userComplaintModel: UserComplaintModel
    private lateinit var loadingDialog: Dialog
    private lateinit var feedBackDialog: Dialog
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var complaintFeedbackDialogBinding: ComplaintFeedbackDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        // Set click listener for feedback button
        binding.feedBack.setOnClickListener(this)

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
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.feedBack -> createFeedBackDialog() // Open feedback dialog on button click
        }
    }

    private fun createFeedBackDialog() {
        // Inflate the layout for feedback dialog
        complaintFeedbackDialogBinding =
            ComplaintFeedbackDialogBinding.inflate(LayoutInflater.from(this@UserComplaintDetailsActivity))
        feedBackDialog = Dialog(this@UserComplaintDetailsActivity)
        feedBackDialog.setContentView(complaintFeedbackDialogBinding.root)
        feedBackDialog.setCancelable(false)
        feedBackDialog.show()
        feedBackDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set existing feedback text to the dialog
        complaintFeedbackDialogBinding.complaintFeedback.setText(userComplaintModel.feedback)

        // Set close button click listener
        complaintFeedbackDialogBinding.closeBtn.setOnClickListener {
            feedBackDialog.dismiss() // Dismiss feedback dialog
        }

        // Set feedback submission button click listener
        complaintFeedbackDialogBinding.complaintFeedbackLayout.setEndIconOnClickListener {
            if (!complaintFeedbackDialogBinding.complaintFeedback.text.isNullOrEmpty()) {
                loadingDialog = LoadingDialog.showLoadingDialog(this@UserComplaintDetailsActivity)!!
                // Send feedback to Firestore database
                sendFeedbackToDb(complaintFeedbackDialogBinding.complaintFeedback.text.toString())
            } else {
                // Show error if feedback is empty
                complaintFeedbackDialogBinding.complaintFeedback.error =
                    "Please enter your feedback"
            }
        }
    }

    private fun sendFeedbackToDb(feedback: String) {
        // Update feedback in Firestore database for the corresponding complaint
        firestoreDb.collection("UserComplaints").document(userComplaintModel.id)
            .update("feedback", feedback).addOnSuccessListener {
                // Hide loading dialog on success
                feedBackDialog.dismiss()
                LoadingDialog.hideLoadingDialog(loadingDialog)
                // Update local userComplaintModel with new feedback
                userComplaintModel.feedback = feedback
                // Show success message
                Toast.makeText(
                    this@UserComplaintDetailsActivity, "Feedback updated", Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                // Show error message on failure
                Toast.makeText(this@UserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
