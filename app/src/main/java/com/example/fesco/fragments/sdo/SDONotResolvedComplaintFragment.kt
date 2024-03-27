package com.example.fesco.fragments.sdo

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
import com.example.fesco.adapters.SDOUserComplaintAdp
import com.example.fesco.databinding.FragmentSDONotResolvedComplaintBinding
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

class SDONotResolvedComplaintFragment : Fragment() {

    private lateinit var binding: FragmentSDONotResolvedComplaintBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var sdoData: SharedPreferences
    private lateinit var adapter: SDOUserComplaintAdp
    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSDONotResolvedComplaintBinding.inflate(inflater, container, false)
        checkNetworkConnectivity()
        return binding.root
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        binding.sdoUserNotResolvedComplaintsRecycler.layoutManager =
            LinearLayoutManager(requireActivity())
        sdoData = requireActivity().getSharedPreferences("sdoData", AppCompatActivity.MODE_PRIVATE)
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
        getSDOUserComplaintsID()
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

    private fun getNotResolvedComplaintsForXEN() {

        if (updatedComplaintList.isNotEmpty()) {

            val notResolvedComplaintList = mutableListOf<String>()

            updatedComplaintList.forEach { complaint ->
                if (complaint.status != "Resolved") {
                    if (getHourDifferenceOfComplaints(complaint.dateTime) >= 48 && !complaint.sentToXEN) {
                        notResolvedComplaintList.add(complaint.id)
                    }
                }
            }
            if (notResolvedComplaintList.isNotEmpty()) {
                getXENPreviousUserComplaintsID(notResolvedComplaintList)
            }
        }
    }

    private fun getXENPreviousUserComplaintsID(notResolvedComplaintList: List<String>) {

        firestoreDb.collection("XEN").document(sdoData.getString("xen", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    val mergedComplaints = (complaints ?: emptyList()).toMutableSet().apply {
                        addAll(notResolvedComplaintList)
                    }.toList()

                    sendNotResolvedComplaintsToXEN(
                        mergedComplaints, notResolvedComplaintList, sdoData.getString("xen", "")!!
                    )

                }
            }
    }

    private fun sendNotResolvedComplaintsToXEN(
        complaintList: List<String>, notResolvedComplaintList: List<String>, xenID: String
    ) {
        firestoreDb.collection("XEN").document(xenID).update("complaints", complaintList)
            .addOnSuccessListener {
                updateComplaintSendXENStatus(notResolvedComplaintList)
                getLSFCMToken(xenID)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLSFCMToken(xenID: String) {
        firestoreDb.collection("XEN").document(xenID).get()
            .addOnSuccessListener {
                sendNotificationToSDO(it.get("xenFCMToken").toString())
            }.addOnFailureListener {
                Toast.makeText(
                    requireActivity(), it.message + " --getXENFCMToken Failure", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendNotificationToSDO(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put(
                        "title",
                        requireActivity().getSharedPreferences("sdoData", Context.MODE_PRIVATE)
                            .getString("name", "")
                    )
                    put("body", "SDO has unresolved complaints.")
                    put("userType", "sdoToXen")
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

    private fun updateComplaintSendXENStatus(notResolvedComplaintList: List<String>) {

        for (complaint in notResolvedComplaintList) {
            firestoreDb.collection("UserComplaints").document(complaint).update("sentToXEN", true)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getHourDifferenceOfComplaints(dateString: String): Long {
        val date = SimpleDateFormat("dd MMM yyyy hh:mm a").parse(dateString)
        return (Calendar.getInstance().timeInMillis - date!!.time) / (1000 * 60 * 60)
    }

    private fun getSDOUserComplaintsID() {

        firestoreDb.collection("SDO").document(sdoData.getString("id", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        getSDOUserComplaintDataFromDb(it)
                    } ?: run {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                    }
                }
            }
    }

    private fun getSDOUserComplaintDataFromDb(complaintList: List<String>) {

        if (complaintList.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle exception
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
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
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        adapter = SDOUserComplaintAdp(requireActivity(), list)
        binding.sdoUserNotResolvedComplaintsRecycler.adapter = adapter

        getNotResolvedComplaintsForXEN()
    }
}