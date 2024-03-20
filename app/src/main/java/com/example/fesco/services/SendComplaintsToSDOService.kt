package com.example.fesco.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.fesco.models.UserComplaintModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SendComplaintsToSDOService : IntentService("SendComplaintsToSDOService") {

    companion object {
        private const val TAG = "SDO_Service"
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Service started")

        // Fetch not resolved complaints from Firestore
        val firestore = FirebaseFirestore.getInstance()
        val notResolvedComplaints = mutableListOf<UserComplaintModel>()

        firestore.collection("UserComplaints")
            .whereEqualTo("status", "In Process")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val complaint = document.toObject(UserComplaintModel::class.java)
                    if (!complaint.sentToSDO) {
                        notResolvedComplaints.add(complaint)
                    }
                }
                // Send not resolved complaints to SDO
                sendComplaintsToSDO(notResolvedComplaints)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting documents: ", e)
            }
    }

    private fun sendComplaintsToSDO(complaints: List<UserComplaintModel>) {
        GlobalScope.launch(Dispatchers.IO) {
            val firestore = FirebaseFirestore.getInstance()

            // Check if there are complaints to send
            if (complaints.isEmpty()) {
                Log.e(TAG, "No unresolved complaints found")
                return@launch
            }

            // Fetch SdoID from Users collection using consumerID from UserComplaintModel
            val sdoID = fetchSdoIDFromUsers(complaints.firstOrNull()?.consumerID ?: "")

            // Check if SDO ID is empty
            if (sdoID.isEmpty()) {
                Log.e(TAG, "SDO ID not found for consumer")
                return@launch
            }

            // Update SDO collection with the list of complaints
            val newComplaints = complaints.map { it.id }
            addComplaintsToSdoList(sdoID, newComplaints)
            Log.d(TAG, "Complaints sent to SDO")

            // Update sendToSDO flags of all complaints in UserComplaints collection
            for (complaint in complaints) {
                complaint.sentToSDO = true
                firestore.collection("UserComplaints").document(complaint.id)
                    .set(complaint)
                    .await()
                Log.d(TAG, "sendToSDO flag updated for complaint: ${complaint.id}")
            }
        }
    }

    private suspend fun fetchSdoIDFromUsers(consumerID: String): String {
        val firestore = FirebaseFirestore.getInstance()
        val document = firestore.collection("Users").document(consumerID).get().await()
        return document.getString("sdo") ?: ""
    }

    private suspend fun addComplaintsToSdoList(sdoID: String, newComplaints: List<String>) {
        val firestore = FirebaseFirestore.getInstance()

        // Fetch existing list of complaints from SDO document
        val document = firestore.collection("SDO").document(sdoID).get().await()
        val existingComplaints = document.get("complaints") as? List<String>

        val mergedComplaints = (existingComplaints ?: emptyList()).toMutableSet().apply {
            addAll(newComplaints)
        }.toList()

        // Update SDO collection with the merged list of complaints
        firestore.collection("SDO").document(sdoID)
            .update("complaints", mergedComplaints)
            .await()
    }
}