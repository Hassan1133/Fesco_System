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

    private lateinit var lsList: MutableList<LSModel>

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
        lsList = mutableListOf<LSModel>()
        binding.lsRecycler.layoutManager = LinearLayoutManager(activity)
        getSDOArrayFromSharedPreferences()
    }

    private fun getSDOArrayFromSharedPreferences() {
        val list = context?.getSharedPreferences("sdoData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("ls", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java).toList() }

        list?.let { getSdoDataFromDb(it) }
    }

    private fun getSdoDataFromDb(list: List<String>) {
        if (list.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        firestoreDb.collection("LS").whereIn("id", list)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                lsList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val ls = documentSnapshot.toObject(LSModel::class.java)
                    ls?.let {
                        ls?.let {
                            lsList.add(it)
                        }
                    }
                }

                setDataToRecycler(lsList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list : List<LSModel>)
    {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        binding.lsRecycler.adapter = SDOLSAdp(requireActivity(),list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }
}