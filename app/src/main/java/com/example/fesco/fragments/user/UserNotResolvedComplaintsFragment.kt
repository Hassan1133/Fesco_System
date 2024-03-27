package com.example.fesco.fragments.user

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fesco.R
import com.example.fesco.adapters.UserComplaintAdp
import com.example.fesco.databinding.ComplaintDialogBinding
import com.example.fesco.databinding.FragmentUserNotReslovedComplaintsBinding
import com.example.fesco.main_utils.LoadingDialog
import com.example.fesco.main_utils.NetworkManager
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
import java.util.Date


class UserNotResolvedComplaintsFragment : Fragment(), OnClickListener {

    // View binding for the fragment layout
    private lateinit var binding: FragmentUserNotReslovedComplaintsBinding

    // View binding for the complaint dialog layout
    private lateinit var userComplaintDialogBinding: ComplaintDialogBinding

    // Loading dialog to show during network operations
    private lateinit var loadingDialog: Dialog

    // Dialog for user to submit a new complaint
    private lateinit var userComplaintDialog: Dialog

    // Firebase Firestore instance
    private lateinit var firestoreDb: FirebaseFirestore

    // Shared preferences to store user data
    private lateinit var userData: SharedPreferences

    // List to hold updated complaint data
    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout
        binding = FragmentUserNotReslovedComplaintsBinding.inflate(layoutInflater, container, false)
        // Check network connectivity
        checkNetworkConnectivity()
        return binding.root
    }

    private fun init() {
        // Set click listener for the add complaint button
        binding.addComplaintBtn.setOnClickListener(this)

        // Initialize Firestore instance
        firestoreDb = Firebase.firestore

        // Initialize the list to hold updated complaint data
        updatedComplaintList = mutableListOf<UserComplaintModel>()

        // Get user data from shared preferences
        userData =
            requireActivity().getSharedPreferences("userData", AppCompatActivity.MODE_PRIVATE)

        // Set layout manager for recycler view
        binding.userComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())

        // Fetch user complaints data
        getUserComplaintsID()

        // Set search functionality for the search view
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
                // Initialize the fragment
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
            // Filter complaints based on search text
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
        // Update recycler view with filtered list
        setDataToRecycler(searchList)
    }

    private fun getHoursDifferenceUpdatedText(dateTime: String): String {
        // Calculate time difference for a complaint
        val dateTimeLong = getHourDifferenceOfComplaints(dateTime)
        return "$dateTimeLong hours"
    }

    @SuppressLint("SimpleDateFormat")
    private fun getHourDifferenceOfComplaints(dateString: String): Long {
        // Calculate time difference in hours
        val date = SimpleDateFormat("dd MMM yyyy hh:mm a").parse(dateString)
        return (Calendar.getInstance().timeInMillis - date!!.time) / (1000 * 60 * 60)
    }

    private fun getUserComplaintsID() {
        // Show loading dialog
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
        // Fetch user complaints data from Firestore
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                }
                snapShot?.let { document ->
                    // Get complaint data from Firestore
                    getComplaintDataFromDb(
                        document.get("complaints") as? List<String> ?: emptyList()
                    )
                }
            }
    }

    private fun getComplaintDataFromDb(complaintList: List<String>) {
        if (complaintList.isEmpty()) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            return
        }

        // Fetch complaint data for each complaint ID
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
                    // Convert document snapshot to UserComplaintModel object
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    if (complaint?.status != "Resolved") {
                        complaint?.let {
                            updatedComplaintList.add(it)
                        }
                    }
                }

                // Sort the updated complaint list by date
                updatedComplaintList.sortByDescending { it.dateTime }
                // Update UI with the updated complaint list
                setDataToRecycler(updatedComplaintList)
                // Hide loading dialog
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }
    }

    private fun setDataToRecycler(list: List<UserComplaintModel>) {
        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }
        binding.userComplaintsRecycler.adapter = UserComplaintAdp(requireActivity(), list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addComplaintBtn -> {
                val networkManager = NetworkManager(requireActivity())
                val isConnected = networkManager.isNetworkAvailable()
                if (isConnected) {
                    createComplaintDialog()
                } else {
                    Toast.makeText(
                        requireActivity(), "Please connect to internet", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createComplaintDialog() {
        userComplaintDialogBinding =
            ComplaintDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        userComplaintDialog = Dialog(requireActivity())
        userComplaintDialog.setContentView(userComplaintDialogBinding.root)
        userComplaintDialog.setCancelable(false)
        userComplaintDialog.show()
        userComplaintDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            getComplaintTypesFromDB()?.let { complaintTypes ->
                userComplaintDialogBinding.complaintType.setAdapter(
                    ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, complaintTypes
                    )
                )
            }
        }

        userComplaintDialogBinding.closeBtn.setOnClickListener {
            userComplaintDialog.dismiss()
        }

        userComplaintDialogBinding.submitBtn.setOnClickListener {
            if (isValid()) {
                loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
                setComplaintDataToModel()
            }
        }
    }

    private fun setComplaintDataToModel() {
        val model = UserComplaintModel()
        model.consumerID = userData.getString("consumerID", "")!!
        model.userName = userData.getString("name", "")!!
        model.address = userData.getString("address", "")!!
        model.phoneNo = userData.getString("phoneNo", "")!!
        model.complaintType = userComplaintDialogBinding.complaintType.text.toString()
        model.dateTime = getCurrentDateTime()!!
        model.status = "Pending"
        model.lm = ""
        model.feedback = "none"
        model.sentToSDO = false
        model.sentToXEN = false

        submitComplaint(model)
    }

    private fun isValid(): Boolean {
        var valid = true
        if (userComplaintDialogBinding.complaintType.text.isNullOrEmpty()) {
            Toast.makeText(requireActivity(), "Please select complaint type", Toast.LENGTH_SHORT)
                .show()
            valid = false
        }
        return valid
    }

    private fun submitComplaint(model: UserComplaintModel) {
        // Get references for user and LS documents
        val userDocRef =
            firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
        val lsDocRef = firestoreDb.collection("LS").document(userData.getString("ls", "")!!)

        // Start a Firestore transaction
        firestoreDb.runTransaction { transaction ->
            // Perform reads inside the transaction
            val userDocSnapshot = transaction.get(userDocRef)
            val lsDocSnapshot = transaction.get(lsDocRef)

            // Extract user and LS complaint lists
            val currentUserComplaints = userDocSnapshot.get("complaints") as? MutableList<String>
                ?: mutableListOf()
            val currentLsComplaints = lsDocSnapshot.get("complaints") as? MutableList<String>
                ?: mutableListOf()

            // Create a new complaint document
            val complaintRef = firestoreDb.collection("UserComplaints").document()
            model.id = complaintRef.id
            transaction.set(complaintRef, model)

            // Update user complaints list
            currentUserComplaints.add(model.id)
            transaction.update(userDocRef, "complaints", currentUserComplaints)

            // Update LS complaints list
            currentLsComplaints.add(model.id)
            transaction.update(lsDocRef, "complaints", currentLsComplaints)

        }.addOnSuccessListener {
            // Retrieve FCM token and send notification (outside of the transaction)
            LoadingDialog.hideLoadingDialog(loadingDialog)
            userComplaintDialog.dismiss()
            getLSFCMToken(userData.getString("ls", "")!!)
        }.addOnFailureListener { exception ->
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun getLSFCMToken(lsId: String) {
        firestoreDb.collection("LS").document(lsId).get().addOnSuccessListener {
            sendNotificationToLS(it.get("lsFCMToken").toString())
        }.addOnFailureListener {
            Toast.makeText(
                requireActivity(), it.message + " --getLSFCMToken Failure", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendNotificationToLS(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("title", userData.getString("name", ""))
                    put("body", "user has registered a complaint.")
                    put("userType", "userToLs")
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
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireActivity(), "Complaint Submitted Successfully", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDateTime(): String? {
        val dateFormat = SimpleDateFormat("d MMM yyyy hh:mm a")
        return dateFormat.format(Date())
    }

    private suspend fun getComplaintTypesFromDB(): List<String>? {
        return try {
            firestoreDb.collection("ComplaintTypes").document("allComplaintTypes").get().await()
                .get("types") as List<String>
        } catch (e: Exception) {
            null
        }
    }
}