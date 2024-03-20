package com.example.fesco.fragments

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.LMUserComplaintAdp
import com.example.fesco.databinding.FragmentLMNotResolvedComplaintBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.models.UserComplaintModel
import com.example.fesco.receivers.AlarmReceiver
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class LMNotResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLMNotResolvedComplaintBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var lmData: SharedPreferences

    private lateinit var adapter: LMUserComplaintAdp

    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLMNotResolvedComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        binding.lmUnResolvedComplaintsRecycler.layoutManager =
            LinearLayoutManager(requireActivity())
        lmData = requireActivity().getSharedPreferences("lmData", AppCompatActivity.MODE_PRIVATE)
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Set up the AlarmManager to trigger every 24 hours
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(requireContext(), 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            60 * 1000,
            alarmIntent
        )


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
                        if (complaint?.status != "Resolved") {
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
        binding.lmUnResolvedComplaintsRecycler.adapter = adapter
    }
}