package com.example.fesco.fragments.sdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.SDOLSAdp
import com.example.fesco.databinding.FragmentSDOLSBinding
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.LSModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class SDOLSFragment : Fragment() {

    private lateinit var binding: FragmentSDOLSBinding

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lsList: MutableList<LSModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSDOLSBinding.inflate(inflater, container, false)
        checkNetworkConnectivity()
        return binding.root
    }

    private fun init() {
        firestoreDb = Firebase.firestore
        lsList = mutableListOf<LSModel>()
        binding.lsRecycler.layoutManager = LinearLayoutManager(activity)
        getSDOArrayFromSharedPreferences()
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

    private fun getSDOArrayFromSharedPreferences() {

        binding.progressbar.visibility = View.VISIBLE

        val list = context?.getSharedPreferences("sdoData", AppCompatActivity.MODE_PRIVATE)
            ?.getString("ls", null)
            ?.let { Gson().fromJson(it, Array<String>::class.java).toList() }

        list?.let { getSdoDataFromDb(it) }
    }

    private fun getSdoDataFromDb(list: List<String>) {
        if (list.isEmpty()) {
            binding.progressbar.visibility = View.GONE
            return
        }

        firestoreDb.collection("LS").whereIn("id", list)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                lsList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val ls = documentSnapshot.toObject(LSModel::class.java)
                    ls?.let {
                        ls.let {
                            lsList.add(it)
                        }
                    }
                }

                setDataToRecycler(lsList)
                binding.progressbar.visibility = View.GONE
            }
    }

    private fun setDataToRecycler(list : List<LSModel>)
    {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        binding.lsRecycler.adapter = SDOLSAdp(requireActivity(),list)
        binding.progressbar.visibility = View.GONE
    }
}