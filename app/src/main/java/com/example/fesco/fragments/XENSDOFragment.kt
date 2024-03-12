package com.example.fesco.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.XENSDOAdp
import com.example.fesco.databinding.FragmentXENSDOBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.SDOModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class XENSDOFragment : Fragment() {

    private lateinit var binding: FragmentXENSDOBinding

    private lateinit var sdoRef: String

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var sdoList: List<SDOModel>

    private lateinit var loadingDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentXENSDOBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        loadingDialog = LoadingDialog.showLoadingDialog(activity)!!
        firestoreDb = Firebase.firestore
        sdoRef = "SDO"
        sdoList = arrayListOf()
        binding.sdoRecycler.layoutManager = LinearLayoutManager(activity)
        getSDOArrayFromSharedPreferences()
    }

    private fun getSDOArrayFromSharedPreferences() {
        val sdoArray = context?.getSharedPreferences("xenData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("sdo", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java) }

        sdoArray?.let { getSdoDataFromDb(it) }
    }

    private fun getSdoDataFromDb(sdoArray: Array<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            sdoList = sdoArray.mapNotNull { sdoID ->
                try {
                    firestoreDb.collection("SDO").document(sdoID).get().await()
                        .toObject(SDOModel::class.java)
                } catch (e: Exception) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    null
                }
            }
            setDataToRecycler(sdoList)
        }
    }

    private fun setDataToRecycler(list : List<SDOModel>)
    {
        binding.sdoRecycler.adapter = XENSDOAdp(requireActivity(),list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}