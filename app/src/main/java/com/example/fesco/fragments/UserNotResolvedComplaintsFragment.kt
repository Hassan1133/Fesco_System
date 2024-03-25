package com.example.fesco.fragments

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
import java.util.Date


class UserNotResolvedComplaintsFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserNotReslovedComplaintsBinding

    private lateinit var userComplaintDialogBinding: ComplaintDialogBinding

    private lateinit var loadingDialog: Dialog

    private lateinit var userComplaintDialog: Dialog

    private lateinit var firestoreDb: FirebaseFirestore

    private lateinit var userData: SharedPreferences

    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserNotReslovedComplaintsBinding.inflate(layoutInflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.addComplaintBtn.setOnClickListener(this)
        firestoreDb = Firebase.firestore
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        userData =
            requireActivity().getSharedPreferences("userData", AppCompatActivity.MODE_PRIVATE)
        binding.userComplaintsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        getUserComplaintsID()

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

    private fun search(newText: String) {
        val searchList = mutableListOf<UserComplaintModel>()
        for (i in updatedComplaintList) {
            if (i.complaintType.lowercase()
                    .contains(newText.lowercase()) || i.dateTime.lowercase()
                    .contains(newText.lowercase()) || i.status.lowercase()
                    .contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        setDataToRecycler(searchList)
    }

    private fun getUserComplaintsID() {
        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                }
                snapShot?.let { document ->
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
                    if (complaint?.status != "Resolved") {
                        complaint?.let {
                            updatedComplaintList.add(it)
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
        binding.userComplaintsRecycler.adapter = UserComplaintAdp(requireActivity(), list)
        LoadingDialog.hideLoadingDialog(loadingDialog)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addComplaintBtn -> createComplaintDialog()
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
                loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())!!
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
        val dbDocument = firestoreDb.collection("UserComplaints").document()
        model.id = dbDocument.id
        dbDocument.set(model).addOnSuccessListener {
            retrieveUserComplaintList(model.id)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveUserComplaintList(id: String) {
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .get()
            .addOnSuccessListener { snapShot ->
                val currentComplaints = snapShot.get("complaints") as? List<String> ?: emptyList()
                val updatedUserComplaints =
                    currentComplaints.filter { it.isNotEmpty() }.toMutableList()
                updatedUserComplaints.add(id)
                sendComplaintIDToUser(id, updatedUserComplaints)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendComplaintIDToUser(id: String, list: List<String>) {
        firestoreDb.collection("Users").document(userData.getString("consumerID", "")!!)
            .update("complaints", list)
            .addOnSuccessListener {
                retrieveLSComplaintList(id)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun retrieveLSComplaintList(id: String) {
        firestoreDb.collection("LS").document(userData.getString("ls", "")!!)
            .get()
            .addOnSuccessListener { snapShot ->
                val currentComplaints = snapShot.get("complaints") as? List<String> ?: emptyList()
                val updatedLSComplaints =
                    currentComplaints.filter { it.isNotEmpty() }.toMutableList()
                updatedLSComplaints.add(id)
                sendComplaintIDToLS(updatedLSComplaints)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendComplaintIDToLS(list: List<String>) {
        firestoreDb.collection("LS").document(userData.getString("ls", "")!!)
            .update("complaints", list)
            .addOnSuccessListener {
                getLSFCMToken(userData.getString("ls", "")!!)
                LoadingDialog.hideLoadingDialog(loadingDialog)
                userComplaintDialog.dismiss()
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(
                    requireActivity(),
                    it.message + " --sendComplaintIDToLS Failure",
                    Toast.LENGTH_SHORT
                ).show()
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
                    put("body", "needs help right now.")
                    put("userType", "user")
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
                requireActivity()?.runOnUiThread {
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                requireActivity()?.runOnUiThread {
                    Toast.makeText(
                        requireActivity(), "Complaint Submitted Successfully", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

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