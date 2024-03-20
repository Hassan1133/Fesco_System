package com.example.fesco.activities

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
        binding.feedBack.setOnClickListener(this)
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
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.feedBack -> createFeedBackDialog()
        }
    }

    private fun createFeedBackDialog() {
        complaintFeedbackDialogBinding =
            ComplaintFeedbackDialogBinding.inflate(LayoutInflater.from(this@UserComplaintDetailsActivity))
        feedBackDialog = Dialog(this@UserComplaintDetailsActivity)
        feedBackDialog.setContentView(complaintFeedbackDialogBinding.root)
        feedBackDialog.setCancelable(false)
        feedBackDialog.show()
        feedBackDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        complaintFeedbackDialogBinding.complaintFeedback.setText(userComplaintModel.feedback)

        complaintFeedbackDialogBinding.closeBtn.setOnClickListener {
            feedBackDialog.dismiss()
        }

        complaintFeedbackDialogBinding.complaintFeedbackLayout.setEndIconOnClickListener {
            if (!complaintFeedbackDialogBinding.complaintFeedback.text.isNullOrEmpty()) {
                loadingDialog = LoadingDialog.showLoadingDialog(this@UserComplaintDetailsActivity)!!
                sendFeedbackToDb(complaintFeedbackDialogBinding.complaintFeedback.text.toString())
            } else {
                complaintFeedbackDialogBinding.complaintFeedback.error =
                    "Please enter your feedback"
            }
        }
    }

    private fun sendFeedbackToDb(feedback: String) {
        firestoreDb.collection("UserComplaints").document(userComplaintModel.id)
            .update("feedback", feedback).addOnSuccessListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userComplaintModel.feedback = feedback
                Toast.makeText(
                    this@UserComplaintDetailsActivity, "Feedback updated", Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(this@UserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }
}