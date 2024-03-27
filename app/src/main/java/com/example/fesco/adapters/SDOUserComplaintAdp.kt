package com.example.fesco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fesco.R
import com.example.fesco.activities.sdo.SDOUserComplaintDetailsActivity
import com.example.fesco.models.UserComplaintModel

class SDOUserComplaintAdp(
    private val context: Context,
    private val complaintList: List<UserComplaintModel>
) : RecyclerView.Adapter<SDOUserComplaintAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_complaint_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userComplaintModel = complaintList[position]

        // Set UserComplaintModel data to the views in the ViewHolder
        holder.complaintType.text = userComplaintModel.complaintType
        holder.complaintDateTime.text = userComplaintModel.dateTime
        holder.complaintStatus.text = userComplaintModel.status

        // Set click listener to handle item click events
        holder.itemView.setOnClickListener {
            val intent = Intent(context, SDOUserComplaintDetailsActivity::class.java)
            intent.putExtra("userComplaintModel", userComplaintModel)
            context.startActivity(intent)
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return complaintList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val complaintType: TextView = itemView.findViewById(R.id.recyclerComplaintType)
        val complaintDateTime: TextView = itemView.findViewById(R.id.recyclerComplaintDateTime)
        val complaintStatus: TextView = itemView.findViewById(R.id.recyclerComplaintStatus)
    }
}
