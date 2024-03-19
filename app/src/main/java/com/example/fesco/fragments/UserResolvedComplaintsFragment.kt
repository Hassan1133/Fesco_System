package com.example.fesco.fragments

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.UserComplaintAdp
import com.example.fesco.databinding.ComplaintDialogBinding
import com.example.fesco.databinding.FragmentUserResolvedComplaintsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserResolvedComplaintsFragment : Fragment() {

    private lateinit var binding: FragmentUserResolvedComplaintsBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var userData: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserResolvedComplaintsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        firestoreDb = Firebase.firestore
        userData = requireActivity().getSharedPreferences("userData", AppCompatActivity.MODE_PRIVATE)
        binding.userSolvedComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())
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
                    if (complaint?.status == "Resolved") {
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
        binding.userSolvedComplaintsRecycler.adapter = UserComplaintAdp(requireActivity(), list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}