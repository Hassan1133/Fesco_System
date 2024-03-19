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
import com.example.fesco.adapters.LSUserComplaintAdp
import com.example.fesco.databinding.FragmentLSComplaintBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore

class LSComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLSComplaintBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lsData: SharedPreferences
    private lateinit var adapter: LSUserComplaintAdp

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLSComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        binding.lsUserComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        lsData = requireActivity().getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
        getLsUserComplaintsID()
    }

    override fun onResume() {
        super.onResume()
        init()
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
                        updatedComplaintList.add(it)
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
        adapter = LSUserComplaintAdp(requireActivity(), list)
        binding.lsUserComplaintsRecycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}