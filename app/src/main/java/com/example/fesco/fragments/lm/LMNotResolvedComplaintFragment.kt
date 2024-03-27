package com.example.fesco.fragments.lm

import android.annotation.SuppressLint
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
import com.example.fesco.adapters.LMUserComplaintAdp
import com.example.fesco.databinding.FragmentLMNotResolvedComplaintBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar

class LMNotResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLMNotResolvedComplaintBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmData: SharedPreferences

    private lateinit var adapter: LMUserComplaintAdp

    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
// Inflate the layout for this fragment
        binding = FragmentLMNotResolvedComplaintBinding.inflate(inflater, container, false)
        // check network connectivity
        checkNetworkConnectivity()
        return binding.root
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        binding.lmUnResolvedComplaintsRecycler.layoutManager =
            LinearLayoutManager(requireActivity())
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        lmData = requireActivity().getSharedPreferences("lmData", AppCompatActivity.MODE_PRIVATE)
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
        // Fetch LM user complaints from Firestore
        getLMUserComplaintsID()
        // Setup search functionality
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

    private fun checkNetworkConnectivity() {
        // Check network connectivity
        val networkManager = NetworkManager(requireActivity())
        try {
            val isConnected = networkManager.isNetworkAvailable()
            if (isConnected) {
                // Initialize fragment components
                init()
            } else {
                Toast.makeText(
                    requireActivity(), "Please connect to the internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            // Handle network check exception
            Toast.makeText(
                requireActivity(), "Network check failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Search for complaints based on user input
    private fun search(newText: String) {
        val searchList = mutableListOf<UserComplaintModel>()
        for (i in updatedComplaintList) {
            if (i.consumerID.contains(newText) || i.userName.lowercase()
                    .contains(newText.lowercase()) || i.phoneNo.contains(newText) || i.address.lowercase()
                    .contains(newText.lowercase()) || i.status.lowercase()
                    .contains(newText.lowercase()) || i.dateTime.lowercase()
                    .contains(newText.lowercase()) || i.complaintType.lowercase()
                    .contains(newText.lowercase()) || i.feedback.lowercase()
                    .contains(newText.lowercase()) || getHoursDifferenceUpdatedText(i.dateTime).lowercase()
                    .contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        // Update RecyclerView with search results
        setDataToRecycler(searchList)
    }

    // Calculate hours difference between complaint date and current date
    private fun getHoursDifferenceUpdatedText(dateTime: String): String {
        val dateTimeLong = getHourDifferenceOfComplaints(dateTime)
        return "$dateTimeLong hours"
    }

    // Get hours difference between two dates
    @SuppressLint("SimpleDateFormat")
    private fun getHourDifferenceOfComplaints(dateString: String): Long {
        val date = SimpleDateFormat("dd MMM yyyy hh:mm a").parse(dateString)
        return (Calendar.getInstance().timeInMillis - date!!.time) / (1000 * 60 * 60)
    }

    // Fetch LM user complaints from Firestore
    private fun getLMUserComplaintsID() {
        firestoreDb.collection("LM").document(lmData.getString("id", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    // Handle Firestore query exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        // Fetch complaint details from Firestore
                        getLMUserComplaintDataFromDb(it)
                    } ?: run {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                    }
                }
            }
    }

    // Fetch details of LM user complaints from Firestore
    private fun getLMUserComplaintDataFromDb(complaintList: List<String>) {
        if (complaintList.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }
        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle Firestore query exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }
                updatedComplaintList.clear()
                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    complaint?.let {
                        if (complaint.status != "Resolved") {
                            // Add unresolved complaints to the list
                            updatedComplaintList.add(it)
                        }
                    }
                }
                // Sort the complaint list by date
                updatedComplaintList.sortByDescending { it.dateTime }
                // Update RecyclerView with complaint data
                setDataToRecycler(updatedComplaintList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    // Update RecyclerView with complaint data
    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        adapter = LMUserComplaintAdp(requireActivity(), list)
        binding.lmUnResolvedComplaintsRecycler.adapter = adapter
    }
}