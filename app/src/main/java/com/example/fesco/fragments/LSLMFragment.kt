package com.example.fesco.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.LSLMAdp
import com.example.fesco.databinding.FragmentLSLMBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.LMModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LSLMFragment : Fragment() {

    private lateinit var binding: FragmentLSLMBinding

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmList: List<LMModel>

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
        lmList = arrayListOf()
        binding.lmRecycler.layoutManager = LinearLayoutManager(activity)
        getLMArrayFromSharedPreferences()
    }

    private fun getLMArrayFromSharedPreferences() {
        val lmArray = context?.getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("lm", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java) }

        lmArray?.let { getLMDataFromDb(it) }
    }

    private fun getLMDataFromDb(lmArray: Array<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            lmList = lmArray.mapNotNull { lmID ->
                try {
                    firestoreDb.collection("LM").document(lmID).get().await()
                        .toObject(LMModel::class.java)
                } catch (e: Exception) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    null
                }
            }
            setDataToRecycler(lmList)
        }
    }

    private fun setDataToRecycler(list : List<LMModel>)
    {
        binding.lmRecycler.adapter = LSLMAdp(requireActivity(),list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}