package com.example.fesco.activities.ls

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fesco.adapters.LMDropDownAdp
import com.example.fesco.databinding.ActivityLsuserComplaintDetailsBinding
import com.example.fesco.interfaces.OnDropDownItemClickListener
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.LMModel
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
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

    private lateinit var binding: ActivityLsuserComplaintDetailsBinding

    private lateinit var userComplaintModel: UserComplaintModel

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmList: List<LMModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLsuserComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        loadingDialog = LoadingDialog.showLoadingDialog(this@LSUserComplaintDetailsActivity)!!
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

        if (userComplaintModel.lm.isEmpty()) {
            getLMArrayFromSharedPreferences()
        } else {
            getLMNameSetToDropDown(userComplaintModel.lm)
        }
    }

    private fun getLMNameSetToDropDown(lm: String) {
        firestoreDb.collection("LM").document(lm).get().addOnSuccessListener {
            val lmName = it.get("name") as? String
            binding.assignToLM.setText(lmName)
            binding.assignToLMLayout.isEnabled = false
            LoadingDialog.hideLoadingDialog(loadingDialog)
        }.addOnFailureListener {
            Toast.makeText(this@LSUserComplaintDetailsActivity, it.message, Toast.LENGTH_SHORT)
                .show()
            LoadingDialog.hideLoadingDialog(loadingDialog)
        }
    }

    private fun getLMArrayFromSharedPreferences() {
        val lmArray =
            getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)?.getString("lm", null)
                ?.let { Gson().fromJson(it, Array<String>::class.java) }

        lmArray?.let { getLMDataFromDb(it) }
    }

    private fun getLMDataFromDb(lmArray: Array<String>) {
        lifecycleScope.launch {
            lmList = lmArray.mapNotNull { lmID ->
                try {
                    firestoreDb.collection("LM").document(lmID).get().await()
                        .toObject(LMModel::class.java)
                } catch (e: Exception) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    null
                }
            }
            getLMNameFromLmList(lmList)
        }
    }

    private fun getLMNameFromLmList(list: List<LMModel>) {

        val adapter = LMDropDownAdp(
            this@LSUserComplaintDetailsActivity,
            list,
            object : OnDropDownItemClickListener {

                override fun onItemClick(lmId: String?, lmName: String?) {
                    loadingDialog =
                        LoadingDialog.showLoadingDialog(this@LSUserComplaintDetailsActivity)!!
                    binding.assignToLM.setText(lmName)
                    binding.assignToLMLayout.isEnabled = false
                    retrieveLMComplaintList(lmId!!)
                }
            })

        binding.assignToLM.setAdapter(adapter)

        LoadingDialog.hideLoadingDialog(loadingDialog)
    }

    private fun retrieveLMComplaintList(lmId: String) {
        firestoreDb.collection("LM").document(lmId).get().addOnSuccessListener { snapShot ->
            val currentComplaints = snapShot.get("complaints") as? List<String> ?: emptyList()
            val updatedComplaints = currentComplaints.filter { it.isNotEmpty() }.toMutableList()
            updatedComplaints.add(userComplaintModel.id)
            sendComplaintIDToLM(updatedComplaints, lmId)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendComplaintIDToLM(list: List<String>, lmId: String) {
        firestoreDb.collection("LM").document(lmId).update("complaints", list)
            .addOnSuccessListener {
                updateComplaintStatus(lmId)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateComplaintStatus(lmId: String) {
        firestoreDb.collection("UserComplaints").document(userComplaintModel.id)
            .update("status", "In Process", "lm", lmId).addOnSuccessListener {
                getLSFCMToken(lmId)
                LoadingDialog.hideLoadingDialog(loadingDialog)
                binding.status.text = "In Process"

            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
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
                    put("body", "needs help right now.")
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