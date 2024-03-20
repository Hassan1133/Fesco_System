package com.example.fesco.fragments

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.R
import com.example.fesco.adapters.LMUserComplaintAdp
import com.example.fesco.databinding.FragmentLMResolvedComplaintBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore

class LMResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLMResolvedComplaintBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmData: SharedPreferences

    private lateinit var adapter: LMUserComplaintAdp

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLMResolvedComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        binding.lmResolvedComplaintsRecycler.layoutManager =
            LinearLayoutManager(requireActivity())
        lmData = requireActivity().getSharedPreferences("lmData", AppCompatActivity.MODE_PRIVATE)
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
        getLMUserComplaintsID()
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun getLMUserComplaintsID() {

        firestoreDb.collection("LM").document(lmData.getString("id", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    var complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        getLMUserComplaintDataFromDb(it)
                    } ?: run {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                    }
                }
            }
    }

    private fun getLMUserComplaintDataFromDb(complaintList: List<String>) {

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

                val updatedComplaintList = mutableListOf<UserComplaintModel>()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    complaint?.let {
                        if (complaint?.status == "Resolved") {
                            complaint?.let {
                                updatedComplaintList.add(it)
                            }
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
        adapter = LMUserComplaintAdp(requireActivity(), list)
        binding.lmResolvedComplaintsRecycler.adapter = adapter
    }
}