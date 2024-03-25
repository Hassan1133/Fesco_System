package com.example.fesco.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.XENSDOAdp
import com.example.fesco.databinding.FragmentXENSDOBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.SDOModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class XENSDOFragment : Fragment() {

    private lateinit var binding: FragmentXENSDOBinding

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var sdoList: MutableList<SDOModel>

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
        sdoList = mutableListOf<SDOModel>()
        binding.sdoRecycler.layoutManager = LinearLayoutManager(activity)
        getSDOArrayFromSharedPreferences()
    }

    private fun getSDOArrayFromSharedPreferences() {
        val list = context?.getSharedPreferences("xenData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("sdo", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java).toList() }

        list?.let { getSdoDataFromDb(it) }
    }

    private fun getSdoDataFromDb(list: List<String>) {

        if (list.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        firestoreDb.collection("SDO").whereIn("id", list)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                sdoList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val sdo = documentSnapshot.toObject(SDOModel::class.java)
                    sdo?.let {
                        sdo?.let {
                            sdoList.add(it)
                        }
                    }
                }

                setDataToRecycler(sdoList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list : List<SDOModel>)
    {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        binding.sdoRecycler.adapter = XENSDOAdp(requireActivity(),list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}