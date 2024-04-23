package com.example.fesco.fragments.lm

import android.annotation.SuppressLint
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
import com.example.fesco.databinding.FragmentLMResolvedComplaintBinding
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar

class LMResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLMResolvedComplaintBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lmData: SharedPreferences
    private lateinit var adapter: LMUserComplaintAdp
    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentLMResolvedComplaintBinding.inflate(inflater, container, false)
        // check network connectivity
        checkNetworkConnectivity()
        return binding.root
    }

    private fun init() {
        // Initialize Firestore instance
        firestoreDb = FirebaseFirestore.getInstance()
        // Set layout manager for RecyclerView
        binding.lmResolvedComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        // Initialize list to hold updated complaint data
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        // Retrieve shared preferences for LM data
        lmData = requireActivity().getSharedPreferences("lmData", AppCompatActivity.MODE_PRIVATE)

        // Fetch user complaints data
        getLMUserComplaintsID()
        // Set query text listener for search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search on text change
                newText?.let { search(it) }
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

    private fun search(newText: String) {
        // Filter complaints based on search text
        val searchList = mutableListOf<UserComplaintModel>()
        for (i in updatedComplaintList) {
            if (i.consumerID.contains(newText) || i.userName.lowercase().contains(newText.lowercase())
                || i.phoneNo.contains(newText) || i.address.lowercase().contains(newText.lowercase())
                || i.status.lowercase().contains(newText.lowercase()) || i.dateTime.lowercase().contains(newText.lowercase())
                || i.complaintType.lowercase().contains(newText.lowercase()) || i.feedback.lowercase().contains(newText.lowercase())
                || getHoursDifferenceUpdatedText(i.dateTime).lowercase().contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        // Update RecyclerView with search results
        setDataToRecycler(searchList)
    }

    private fun getHoursDifferenceUpdatedText(dateTime: String): String {
        // Calculate hours difference from current time
        val dateTimeLong = getHourDifferenceOfComplaints(dateTime)
        return "$dateTimeLong hours"
    }

    @SuppressLint("SimpleDateFormat")
    private fun getHourDifferenceOfComplaints(dateString: String): Long {
        // Parse complaint date and calculate difference in hours
        val date = SimpleDateFormat("dd MMM yyyy hh:mm a").parse(dateString)
        return (Calendar.getInstance().timeInMillis - date!!.time) / (1000 * 60 * 60)
    }

    private fun getLMUserComplaintsID() {

        binding.progressbar.visibility = View.VISIBLE

        // Retrieve LM user complaints from Firestore
        firestoreDb.collection("LM").document(lmData.getString("id", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    // Handle Firestore exceptions
                    binding.progressbar.visibility = View.GONE
                    activity?.let {
                        Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        // Fetch complaint data based on IDs
                        getLMUserComplaintDataFromDb(it)
                    } ?: run {
                        // Hide loading dialog if no complaints found
                        binding.progressbar.visibility = View.GONE
                    }
                }
            }
    }

    private fun getLMUserComplaintDataFromDb(complaintList: List<String>) {
        // Fetch complaint details from Firestore
        if (complaintList.isEmpty()) {
            // Hide loading dialog if no complaints found
            binding.progressbar.visibility = View.GONE
            return
        }

        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle Firestore exceptions
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                updatedComplaintList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    complaint?.let {
                        if (complaint.status == "Resolved") {
                            // Add resolved complaints to the list
                            updatedComplaintList.add(it)
                        }
                    }
                }

                // Sort complaints by date-time and update RecyclerView
                updatedComplaintList.sortByDescending { it.dateTime }
                setDataToRecycler(updatedComplaintList)
                binding.progressbar.visibility = View.GONE
            }
    }

    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        // Set adapter for RecyclerView
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        adapter = LMUserComplaintAdp(requireActivity(), list)
        binding.lmResolvedComplaintsRecycler.adapter = adapter
    }
}
