package com.example.fesco.activities.ls

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fesco.adapters.LMDropDownAdp
import com.example.fesco.databinding.ActivityLsuserComplaintDetailsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.LMModel
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LSUserComplaintDetailsActivity : AppCompatActivity() {

    // lateinit properties to be initialized later
    private lateinit var binding: ActivityLsuserComplaintDetailsBinding
    private lateinit var userComplaintModel: UserComplaintModel
    private lateinit var loadingDialog: Dialog
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lmList: List<LMModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLsuserComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init() // Initialize the activity on resume
    }

    private fun init() {
        lmList = mutableListOf()
        loadingDialog =
            LoadingDialog.showLoadingDialog(this@LSUserComplaintDetailsActivity)!! // Show loading dialog
        firestoreDb = Firebase.firestore // Initialize Firestore instance
        getDataFromIntentSetToFields() // Get complaint details from intent
    }

    private fun getDataFromIntentSetToFields() {
        userComplaintModel = intent.getSerializableExtra("userComplaintModel") as UserComplaintModel
        // Set complaint details to UI elements
        binding.name.text = userComplaintModel.userName
        binding.consumerID.text = userComplaintModel.consumerID
        binding.dateTime.text = userComplaintModel.dateTime
        binding.complaintType.text = userComplaintModel.complaintType
        binding.address.text = userComplaintModel.address
        binding.status.text = userComplaintModel.status
        binding.phone.text = userComplaintModel.phoneNo
        binding.feedback.text = userComplaintModel.feedback

        // Check if LM details are available in the model object
        if (userComplaintModel.lm.isEmpty()) {
            getLMArrayFromSharedPreferences() // Get LM list from shared preferences
        } else {
            getLMNameSetToDropDown(userComplaintModel.lm) // Get LM name from model object
        }
    }

    private fun getLMNameSetToDropDown(lm: String) {
        // Fetch LM details from Firestore based on ID
        firestoreDb.collection("LM").document(lm).get().addOnSuccessListener { documentSnapshot ->
            val lmName = documentSnapshot.get("name") as? String
            binding.assignToLM.setText(lmName) // Set LM name to dropdown
            binding.assignToLMLayout.isEnabled = false // Disable dropdown if LM is assigned
            LoadingDialog.hideLoadingDialog(loadingDialog) // Hide loading dialog
        }.addOnFailureListener { e ->
            Toast.makeText(this@LSUserComplaintDetailsActivity, e.message, Toast.LENGTH_SHORT)
                .show() // Show error message on failure
            LoadingDialog.hideLoadingDialog(loadingDialog) // Hide loading dialog
        }
    }

    private fun getLMArrayFromSharedPreferences() {
        val lmArrayString =
            getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)?.getString(
                "lm", null
            ) // Get LM data as JSON string
        val lmArray = lmArrayString?.let {
            Gson().fromJson(
                it, Array<String>::class.java
            )
        } // Convert JSON to array

        // Check if LM array is retrieved successfully
        lmArray?.let { getLMDataFromDb(it) } // Get LM data from Firestore using array
    }

    private fun getLMDataFromDb(lmArray: Array<String>) {
        lifecycleScope.launch { // Use coroutine for asynchronous operations
            lmList = lmArray.mapNotNull { lmID -> // Map LM IDs to LM objects
                try {
                    firestoreDb.collection("LM").document(lmID).get().await()
                        .toObject(LMModel::class.java)
                } catch (e: Exception) {
                    LoadingDialog.hideLoadingDialog(loadingDialog) // Hide loading dialog on error
                    null
                }
            }
            getLMNameFromLmList(lmList) // Get LM names from retrieved data
        }
    }
    private fun getLMNameFromLmList(list: List<LMModel>) {

        val adapter = LMDropDownAdp( // Create adapter for LM dropdown
            this@LSUserComplaintDetailsActivity,
            list
        )

        binding.assignToLM.setAdapter(adapter) // Set adapter to dropdown

        binding.assignToLM.setOnItemClickListener { _, _, position, _ ->
            val lmModel = adapter.getItem(position)
            lmModel?.let {
                binding.assignToLM.setText(it.name)
                binding.assignToLMLayout.isEnabled = false // Disable dropdown if LM is assigned

                retrieveLMComplaintList(it.id)

            }
        }

        // Hide loading dialog after adapter is set,
        // assuming selection might happen without network issues
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }

    private fun retrieveLMComplaintList(lmId: String) {
        val complaintRef = firestoreDb.collection("UserComplaints").document(userComplaintModel.id)
        val lmRef = firestoreDb.collection("LM").document(lmId)

        firestoreDb.runTransaction { transaction ->
            val currentComplaints =
                transaction.get(lmRef).data?.get("complaints") as? List<String> ?: emptyList()
            val updatedComplaints = currentComplaints.filter { it.isNotEmpty() }.toMutableList()
            updatedComplaints.add(userComplaintModel.id)

            // Update LM complaints list within the transaction
            transaction.update(lmRef, "complaints", updatedComplaints)

            // Update complaint status within the transaction
            transaction.update(complaintRef, "status", "In Process", "lm", lmId)

            return@runTransaction Unit // Indicate successful transaction
        }.addOnSuccessListener {
            getLSFCMToken(lmId)
            LoadingDialog.hideLoadingDialog(loadingDialog)
            binding.status.text = "In Process"
        }.addOnFailureListener { exception ->
            LoadingDialog.hideLoadingDialog(loadingDialog)
            // Handle potential transaction errors
            if (exception is FirebaseFirestoreException) {
                when (exception.code) {
                    FirebaseFirestoreException.Code.ABORTED -> {
                        Toast.makeText(
                            this@LSUserComplaintDetailsActivity,
                            "Transaction aborted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                        Toast.makeText(
                            this@LSUserComplaintDetailsActivity,
                            "Complaint or LM data might have changed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getLSFCMToken(lmId: String) {
        firestoreDb.collection("LM").document(lmId).get().addOnSuccessListener {
            sendNotificationToLM(it.get("lmFCMToken").toString())
        }.addOnFailureListener {
            Toast.makeText(
                this@LSUserComplaintDetailsActivity, it.message + " --getLMFCMToken Failure", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendNotificationToLM(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("title", getSharedPreferences("lsData", Context.MODE_PRIVATE).getString("name",""))
                    put("body", "LS has assigned you a complaint.")
                    put("userType", "lsToLm")
                }
                put("data", dataObj)
                put("to", token)
            }
            callApi(jsonObject)
        } catch (e: Exception) {
            // Handle exception
        }
    }

    private fun callApi(jsonObject: JSONObject) {
        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body: RequestBody = jsonObject.toString().toRequestBody(json)
        val request: Request = Request.Builder().url(url).post(body).header(
            "Authorization",
            "Bearer AAAAEsCZH-k:APA91bEfvGZwHlw0XZMK9C8o70UmyK4QkVVDMDbLls4Wi2FlUP4Fdm5fe0Y7xyzSKbadMZD-eRlMI3j281K_mxa16qw0J8qeqFhPqEjHHq6ITKOY9sWIck1KpZdwfNeFZWJwOSSL6CZW"
        ).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                this@LSUserComplaintDetailsActivity.runOnUiThread {
                    Toast.makeText(this@LSUserComplaintDetailsActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                this@LSUserComplaintDetailsActivity.runOnUiThread {
                    Toast.makeText(this@LSUserComplaintDetailsActivity, "Complaint assigned to LM successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

}