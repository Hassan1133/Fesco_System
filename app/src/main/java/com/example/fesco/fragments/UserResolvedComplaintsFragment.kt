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
import com.example.fesco.adapters.UserComplaintAdp
import com.example.fesco.databinding.FragmentUserResolvedComplaintsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Locale

class UserResolvedComplaintsFragment : Fragment() {

    private lateinit var binding: FragmentUserResolvedComplaintsBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var userData: SharedPreferences

    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserResolvedComplaintsBinding.inflate(layoutInflater, container, false)
        init()
        return binding.root
    }


    private fun init() {
        firestoreDb = Firebase.firestore
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        userData =
            requireActivity().getSharedPreferences("userData", AppCompatActivity.MODE_PRIVATE)
        binding.userSolvedComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        getUserComplaintsID()

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
            if (i.complaintType.lowercase(Locale.getDefault())
                    .contains(newText.lowercase()) || i.dateTime.lowercase()
                    .contains(newText.lowercase()) || i.status.lowercase()
                    .contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        setDataToRecycler(searchList)
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
                    if (complaint?.status == "Resolved") {
                        complaint?.let {
                            updatedComplaintList.add(it)
                        }
                    }
                }

                updatedComplaintList.sortByDescending { it.dateTime }
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
        binding.userSolvedComplaintsRecycler.adapter = UserComplaintAdp(requireActivity(), list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}