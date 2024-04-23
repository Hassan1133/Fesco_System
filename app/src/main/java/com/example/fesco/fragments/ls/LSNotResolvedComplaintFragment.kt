package com.example.fesco.fragments.ls

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.adapters.LSUserComplaintAdp
import com.example.fesco.databinding.FragmentLSNotResolvedComplaintBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar

class LSNotResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLSNotResolvedComplaintBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lsData: SharedPreferences
    private lateinit var adapter: LSUserComplaintAdp
    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLSNotResolvedComplaintBinding.inflate(inflater, container, false)
        checkNetworkConnectivity()
        return binding.root
    }
    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        binding.lsUserNotResolvedComplaintsRecycler.layoutManager =
            LinearLayoutManager(requireActivity())
        lsData = requireActivity().getSharedPreferences("lsData", AppCompatActivity.MODE_PRIVATE)
        getLsUserComplaintsID()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText!!)
                return true
            }
        })
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

    private fun search(newText: String) {
        val searchList = mutableListOf<UserComplaintModel>()
        for (i in updatedComplaintList) {

            if (i.consumerID.contains(newText) || i.userName.lowercase()
                    .contains(newText.lowercase()) || i.phoneNo.contains(newText) || i.address.lowercase()
                    .contains(newText.lowercase()) || i.status.lowercase()
                    .contains(newText.lowercase()) || i.dateTime.lowercase()
                    .contains(newText.lowercase()) || i.complaintType.lowercase()
                    .contains(newText.lowercase()) || i.feedback.lowercase()
                    .contains(newText.lowercase()) || getHoursDifferenceUpdatedText(i.dateTime).lowercase()
                    .contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        setDataToRecycler(searchList)
    }

    private fun getHoursDifferenceUpdatedText(dateTime: String): String {
        val dateTimeLong = getHourDifferenceOfComplaints(dateTime)

        return "$dateTimeLong hours"
    }
    private fun getNotResolvedComplaintsForSDO() {

        if (updatedComplaintList.isNotEmpty()) {

            val notResolvedComplaintList = mutableListOf<String>()

            updatedComplaintList.forEach { complaint ->
                if (complaint.status != "Resolved") {
                    if (getHourDifferenceOfComplaints(complaint.dateTime) >= 24 && !complaint.sentToSDO) {
                        notResolvedComplaintList.add(complaint.id)
                    }
                }
            }
            if (notResolvedComplaintList.isNotEmpty()) {
                getSDOPreviousUserComplaintsID(notResolvedComplaintList)
            }
        }
    }

    private fun getSDOPreviousUserComplaintsID(notResolvedComplaintList: List<String>) {

        firestoreDb.collection("SDO").document(lsData.getString("sdo", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    val mergedComplaints = (complaints ?: emptyList()).toMutableSet().apply {
                        addAll(notResolvedComplaintList)
                    }.toList()

                    sendNotResolvedComplaintsToSDO(
                        mergedComplaints, notResolvedComplaintList, lsData.getString("sdo", "")!!
                    )

                }
            }
    }

    private fun sendNotResolvedComplaintsToSDO(
        complaintList: List<String>, notResolvedComplaintList: List<String>, sdoID: String
    ) {
        firestoreDb.collection("SDO").document(sdoID).update("complaints", complaintList)
            .addOnSuccessListener {
                updateComplaintSendSDOStatus(notResolvedComplaintList)
                getLSFCMToken(sdoID)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateComplaintSendSDOStatus(notResolvedComplaintList: List<String>) {

        for (complaint in notResolvedComplaintList) {
            firestoreDb.collection("UserComplaints").document(complaint).update("sentToSDO", true)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                }
        }


    }

    private fun getLSFCMToken(sdoId: String) {
        firestoreDb.collection("SDO").document(sdoId).get()
            .addOnSuccessListener {
                sendNotificationToSDO(it.get("sdoFCMToken").toString())
            }.addOnFailureListener {
                Toast.makeText(
                    requireActivity(), it.message + " --getSDOFCMToken Failure", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendNotificationToSDO(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put(
                        "title",
                        requireActivity().getSharedPreferences("lsData", Context.MODE_PRIVATE)
                            .getString("name", "")
                    )
                    put("body", "LS has unresolved complaints.")
                    put("userType", "lsToSdo")
                }
                put("data", dataObj)
                put("to", token)
            }
            callApi(jsonObject)
        } catch (e: Exception) {
            // Handle exception
        }
    }

    private fun callApi(jsonObject: JSONObject) {
        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body: RequestBody = jsonObject.toString().toRequestBody(json)
        val request: Request = Request.Builder().url(url).post(body).header(
            "Authorization",
            "Bearer AAAAEsCZH-k:APA91bEfvGZwHlw0XZMK9C8o70UmyK4QkVVDMDbLls4Wi2FlUP4Fdm5fe0Y7xyzSKbadMZD-eRlMI3j281K_mxa16qw0J8qeqFhPqEjHHq6ITKOY9sWIck1KpZdwfNeFZWJwOSSL6CZW"
        ).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireActivity(),
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getHourDifferenceOfComplaints(dateString: String): Long {
        val date = SimpleDateFormat("dd MMM yyyy hh:mm a").parse(dateString)
        return (Calendar.getInstance().timeInMillis - date!!.time) / (1000 * 60 * 60)
    }
    private fun getLsUserComplaintsID() {

        binding.progressbar.visibility = View.VISIBLE

        firestoreDb.collection("LS").document(lsData.getString("id", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        getLsUserComplaintDataFromDb(it)
                    } ?: run {
                        binding.progressbar.visibility = View.GONE
                    }
                }
            }
    }

    private fun getLsUserComplaintDataFromDb(complaintList: List<String>) {

        if (complaintList.isEmpty()) {
            binding.progressbar.visibility = View.GONE
            return
        }

        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                updatedComplaintList.clear()

                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    complaint?.let {
                        if (complaint.status != "Resolved") {
                            complaint.let {
                                updatedComplaintList.add(it)
                            }
                        }
                    }
                }

                // Update the UI with the updated complaint list
                updatedComplaintList.sortByDescending { it.dateTime }
                setDataToRecycler(updatedComplaintList)
                binding.progressbar.visibility = View.GONE
            }
    }

    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        adapter = LSUserComplaintAdp(requireActivity(), list)
        binding.lsUserNotResolvedComplaintsRecycler.adapter = adapter

        getNotResolvedComplaintsForSDO()
    }
}