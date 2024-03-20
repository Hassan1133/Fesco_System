package com.example.fesco.activities

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
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

class LSUserComplaintDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLsuserComplaintDetailsBinding

    private lateinit var userComplaintModel: UserComplaintModel

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmList: List<LMModel>

    private lateinit var userComplaintList: ArrayList<UserComplaintModel>

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
        userComplaintList = arrayListOf()
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

        if (userComplaintModel.lm.isNullOrEmpty()) {
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
                    updateComplaintStatus(lmId)
                }
            })

        binding.assignToLM.setAdapter(adapter)

        LoadingDialog.hideLoadingDialog(loadingDialog)
    }

    private fun updateComplaintStatus(lmId: String?) {
        firestoreDb.collection("UserComplaints").document(userComplaintModel.id)
            .update("status", "In Process", "lm", lmId).addOnSuccessListener {
                retrieveLMComplaintList(lmId!!)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(this, "Complaint assigned to LM successfully", Toast.LENGTH_SHORT)
                    .show()
                LoadingDialog.hideLoadingDialog(loadingDialog)
                binding.status.text = "In Process"

            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

}