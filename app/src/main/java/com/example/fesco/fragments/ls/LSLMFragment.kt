package com.example.fesco.fragments.ls

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.LSLMAdp
import com.example.fesco.databinding.FragmentLSLMBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.LMModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class LSLMFragment : Fragment() {

    private lateinit var binding: FragmentLSLMBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lmList: MutableList<LMModel>
    private lateinit var loadingDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLSLMBinding.inflate(inflater, container, false)
        // Check internet connectivity
        checkNetworkConnectivity()
        return binding.root
    }

    private fun init() {

        // Initialize Firestore instance
        firestoreDb = Firebase.firestore
        // Initialize list to hold LM data
        lmList = mutableListOf()
        // Set layout manager for RecyclerView
        binding.lmRecycler.layoutManager = LinearLayoutManager(activity)

        // Show loading dialog
        loadingDialog = LoadingDialog.showLoadingDialog(activity)
        // Retrieve LM data from SharedPreferences
        getLMArrayFromSharedPreferences()
    }

    private fun checkNetworkConnectivity() {
        // Check network connectivity
        val networkManager = NetworkManager(requireActivity())
        try {
            val isConnected = networkManager.isNetworkAvailable()
            if (isConnected) {
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

    private fun getLMArrayFromSharedPreferences() {
        // Retrieve LM IDs from SharedPreferences
        val list = context?.getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("lm", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java).toList() }

        // If LM IDs are available, fetch LM data from Firestore
        list?.let { getLMDataFromDb(it) }
    }

    private fun getLMDataFromDb(list: List<String>) {
        if (list.isEmpty()) {
            // Hide loading dialog if LM IDs list is empty
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        firestoreDb.collection("LM").whereIn("id", list)
            .get()
            .addOnSuccessListener { snapshots ->
                lmList.clear()
                // Iterate through LM documents and populate the list
                for (document in snapshots) {
                    val lm = document.toObject(LMModel::class.java)
                    lmList.add(lm)
                }
                // Set data to RecyclerView
                setDataToRecycler(lmList)
                // Hide loading dialog
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
            .addOnFailureListener { exception ->
                // Handle Firestore exception
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setDataToRecycler(list: List<LMModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        // Set adapter for RecyclerView
        binding.lmRecycler.adapter = LSLMAdp(requireActivity(), list)
    }
}
