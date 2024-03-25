package com.example.fesco.fragments

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.LSUserComplaintAdp
import com.example.fesco.databinding.FragmentLSNotResolvedComplaintBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar

class LSNotResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLSNotResolvedComplaintBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lsData: SharedPreferences
    private lateinit var adapter: LSUserComplaintAdp
    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLSNotResolvedComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        binding.lsUserNotResolvedComplaintsRecycler.layoutManager =
            LinearLayoutManager(requireActivity())
        lsData = requireActivity().getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
        getLsUserComplaintsID()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText!!)
                return true
            }
        })
    }

    private fun search(newText: String) {
        val searchList = mutableListOf<UserComplaintModel>()
        for (i in updatedComplaintList) {
            if (i.complaintType.lowercase()
                    .contains(newText.lowercase()) || i.dateTime.lowercase()
                    .contains(newText.lowercase()) || i.status.lowercase()
                    .contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        setDataToRecycler(searchList)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun getNotResolvedComplaintsForSDO() {

        if (!updatedComplaintList.isNullOrEmpty()) {

            val notResolvedComplaintList = mutableListOf<String>()

            updatedComplaintList.forEach { complaint ->
                if (complaint.status != "Resolved") {
                    if (getHourDifferenceOfComplaints(complaint.dateTime) >= 24 && !complaint.sentToSDO) {
                        notResolvedComplaintList.add(complaint.id)
                    }
                }
            }
            if (!notResolvedComplaintList.isNullOrEmpty()) {
                getSDOPreviousUserComplaintsID(notResolvedComplaintList)
            }
        }
    }

    private fun getSDOPreviousUserComplaintsID(notResolvedComplaintList: List<String>) {

        firestoreDb.collection("SDO").document(lsData.getString("sdo", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    var complaints = document.get("complaints") as? List<String>
                    val mergedComplaints = (complaints ?: emptyList()).toMutableSet().apply {
                        addAll(notResolvedComplaintList)
                    }.toList()

                    sendNotResolvedComplaintsToSDO(
                        mergedComplaints, notResolvedComplaintList, lsData.getString("sdo", "")!!
                    )

                }
            }
    }

    private fun sendNotResolvedComplaintsToSDO(
        complaintList: List<String>, notResolvedComplaintList: List<String>, sdoID: String
    ) {
        firestoreDb.collection("SDO").document(sdoID).update("complaints", complaintList)
            .addOnSuccessListener {
                updateComplaintSendSDOStatus(notResolvedComplaintList)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateComplaintSendSDOStatus(notResolvedComplaintList: List<String>) {

        for (complaint in notResolvedComplaintList) {
            firestoreDb.collection("UserComplaints").document(complaint).update("sentToSDO", true)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getHourDifferenceOfComplaints(dateString: String): Long {
        val date = SimpleDateFormat("dd MMM yyyy hh:mm a").parse(dateString)
        return (Calendar.getInstance().timeInMillis - date.time) / (1000 * 60 * 60)
    }
    private fun getLsUserComplaintsID() {

        firestoreDb.collection("LS").document(lsData.getString("id", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    var complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        getLsUserComplaintDataFromDb(it)
                    } ?: run {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                    }
                }
            }
    }

    private fun getLsUserComplaintDataFromDb(complaintList: List<String>) {

        if (complaintList.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                updatedComplaintList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    complaint?.let {
                        if (complaint?.status != "Resolved") {
                            complaint?.let {
                                updatedComplaintList.add(it)
                            }
                        }
                    }
                }

                // Update the UI with the updated complaint list
                updatedComplaintList.sortByDescending { it.dateTime }
                setDataToRecycler(updatedComplaintList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        adapter = LSUserComplaintAdp(requireActivity(), list)
        binding.lsUserNotResolvedComplaintsRecycler.adapter = adapter

        getNotResolvedComplaintsForSDO()
    }
}