package com.example.fesco.fragments

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.R
import com.example.fesco.adapters.UserComplaintAdp
import com.example.fesco.databinding.ComplaintDialogBinding
import com.example.fesco.databinding.FragmentUserPendingComplaintsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class UserPendingComplaintsFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserPendingComplaintsBinding

    private lateinit var userComplaintDialogBinding: ComplaintDialogBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var userComplaintDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var userData: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserPendingComplaintsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        binding.addComplaintBtn.setOnClickListener(this)
        firestoreDb = Firebase.firestore
        userData =
            requireActivity().getSharedPreferences("userData", AppCompatActivity.MODE_PRIVATE)
        binding.userComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        getUserComplaintsID()
    }

    private fun getUserComplaintsID() {
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                }
                snapShot?.let { document ->
                    getComplaintDataFromDb(
                        document.get("complaints") as? List<String> ?: emptyList()
                    )
                }
            }
    }

    private fun getComplaintDataFromDb(complaintList: List<String>) {
        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                val updatedComplaintList = mutableListOf<UserComplaintModel>()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    if (complaint?.status != "Resolved") {
                        complaint?.let {
                            updatedComplaintList.add(it)
                        }
                    }
                }


                // Update the UI with the updated complaint list
                setDataToRecycler(updatedComplaintList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        binding.userComplaintsRecycler.adapter = UserComplaintAdp(requireActivity(), list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.addComplaintBtn -> createComplaintDialog()
        }
    }

    private fun createComplaintDialog() {
        userComplaintDialogBinding =
            ComplaintDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        userComplaintDialog = Dialog(requireActivity())
        userComplaintDialog.setContentView(userComplaintDialogBinding.root)
        userComplaintDialog.setCancelable(false)
        userComplaintDialog.show()
        userComplaintDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            getComplaintTypesFromDB()?.let { complaintTypes ->
                userComplaintDialogBinding.complaintType.setAdapter(
                    ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, complaintTypes
                    )
                )
            }
        }

        userComplaintDialogBinding.closeBtn.setOnClickListener {
            userComplaintDialog.dismiss()
        }

        userComplaintDialogBinding.submitBtn.setOnClickListener {
            if (isValid()) {
                loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
                setComplaintDataToModel()
            }
        }
    }

    private fun setComplaintDataToModel() {
        val model = UserComplaintModel()
        model.consumerID = userData.getString("consumerID", "")!!
        model.userName = userData.getString("name", "")!!
        model.address = userData.getString("address", "")!!
        model.phoneNo = userData.getString("phoneNo", "")!!
        model.complaintType = userComplaintDialogBinding.complaintType.text.toString()
        model.dateTime = getCurrentDateTime()!!
        model.status = "Pending"
        model.lm = ""

        submitComplaint(model)
    }

    private fun isValid(): Boolean {
        var valid = true
        if (userComplaintDialogBinding.complaintType.text.isNullOrEmpty()) {
            Toast.makeText(requireActivity(), "Please select complaint type", Toast.LENGTH_SHORT)
                .show()
            valid = false
        }
        return valid
    }

    private fun submitComplaint(model: UserComplaintModel) {
        val dbDocument = firestoreDb.collection("UserComplaints").document()
        model.id = dbDocument.id
        dbDocument.set(model).addOnSuccessListener {
            retrieveUserComplaintList(model.id)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveUserComplaintList(id: String) {
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .get()
            .addOnSuccessListener { snapShot ->
                val currentComplaints = snapShot.get("complaints") as? List<String> ?: emptyList()
                val updatedComplaints = currentComplaints.filter { it.isNotEmpty() }.toMutableList()
                updatedComplaints.add(id)
                sendComplaintIDToUser(id, updatedComplaints)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendComplaintIDToUser(id: String, list: List<String>) {
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .update("complaints", list)
            .addOnSuccessListener {
                retrieveLSComplaintList(id)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun retrieveLSComplaintList(id: String) {
        firestoreDb.collection("LS").document(userData.getString("ls", "")!!)
            .get()
            .addOnSuccessListener { snapShot ->
                val currentComplaints = snapShot.get("complaints") as? List<String> ?: emptyList()
                val updatedComplaints = currentComplaints.filter { it.isNotEmpty() }.toMutableList()
                updatedComplaints.add(id)
                sendComplaintIDToLS(updatedComplaints)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendComplaintIDToLS(list: List<String>) {
        firestoreDb.collection("LS").document(userData.getString("ls", "")!!)
            .update("complaints", list)
            .addOnSuccessListener {
                Toast.makeText(
                    requireActivity(),
                    "Complaint Submitted Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userComplaintDialog.dismiss()
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentDateTime(): String? {
        val dateFormat = SimpleDateFormat("d MMM yyyy hh:mm a")
        return dateFormat.format(Date())
    }

    private suspend fun getComplaintTypesFromDB(): List<String>? {
        return try {
            firestoreDb.collection("ComplaintTypes").document("allComplaintTypes").get().await()
                .get("types") as List<String>
        } catch (e: Exception) {
            null
        }
    }
}