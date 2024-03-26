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
    ): View? {
        binding = FragmentLSLMBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        loadingDialog = LoadingDialog.showLoadingDialog(activity)!!
        firestoreDb = Firebase.firestore
        lmList = mutableListOf<LMModel>()
        binding.lmRecycler.layoutManager = LinearLayoutManager(activity)
        getLMArrayFromSharedPreferences()
    }

    private fun getLMArrayFromSharedPreferences() {
        val list = context?.getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("lm", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java).toList() }

        list?.let { getLMDataFromDb(it) }
    }

    private fun getLMDataFromDb(list: List<String>) {
        if (list.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        firestoreDb.collection("LM").whereIn("id", list)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                lmList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val lm = documentSnapshot.toObject(LMModel::class.java)
                    lm?.let {
                        lm?.let {
                            lmList.add(it)
                        }
                    }
                }

                setDataToRecycler(lmList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list : List<LMModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        binding.lmRecycler.adapter = LSLMAdp(requireActivity(), list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}