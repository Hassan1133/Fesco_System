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
import com.example.fesco.adapters.SDOLSAdp
import com.example.fesco.adapters.XENSDOAdp
import com.example.fesco.databinding.FragmentSDOLSBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.LSModel
import com.example.fesco.models.SDOModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SDOLSFragment : Fragment() {

    private lateinit var binding: FragmentSDOLSBinding

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lsList: List<LSModel>

    private lateinit var loadingDialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSDOLSBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        loadingDialog = LoadingDialog.showLoadingDialog(activity)!!
        firestoreDb = Firebase.firestore
        lsList = arrayListOf()
        binding.lsRecycler.layoutManager = LinearLayoutManager(activity)
        getSDOArrayFromSharedPreferences()
    }

    private fun getSDOArrayFromSharedPreferences() {
        val lsArray = context?.getSharedPreferences("sdoData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("ls", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java) }

        lsArray?.let { getSdoDataFromDb(it) }
    }

    private fun getSdoDataFromDb(lsArray: Array<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            lsList = lsArray.mapNotNull { lsID ->
                try {
                    firestoreDb.collection("LS").document(lsID).get().await()
                        .toObject(LSModel::class.java)
                } catch (e: Exception) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    null
                }
            }
            setDataToRecycler(lsList)
        }
    }

    private fun setDataToRecycler(list : List<LSModel>)
    {
        binding.lsRecycler.adapter = SDOLSAdp(requireActivity(),list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}