package com.example.fesco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fesco.R
import com.example.fesco.activities.xen.XENUserComplaintDetailsActivity
import com.example.fesco.models.UserComplaintModel

class XENUserComplaintAdp(private val context: Context, private val complaintList: List<UserComplaintModel>) :
    RecyclerView.Adapter<XENUserComplaintAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_complaint_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds the data to the views in each item of the RecyclerView
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val userComplaintModel = complaintList[position]

        // Set the complaint type, date time, and status to their respective TextViews
        holder.complaintType.text = userComplaintModel.complaintType
        holder.complaintDateTime.text = userComplaintModel.dateTime
        holder.complaintStatus.text = userComplaintModel.status

        // Handle item click to navigate to complaint details activity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, XENUserComplaintDetailsActivity::class.java)
            intent.putExtra("userComplaintModel", userComplaintModel)
            context.startActivity(intent)
        }
    }

    // Returns the total number of items in the RecyclerView
    override fun getItemCount(): Int {
        return complaintList.size
    }

    // ViewHolder class holds the views for each item in the RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val complaintType: TextView = itemView.findViewById(R.id.recyclerComplaintType) // TextView for complaint type
        val complaintDateTime: TextView = itemView.findViewById(R.id.recyclerComplaintDateTime) // TextView for complaint date time
        val complaintStatus: TextView = itemView.findViewById(R.id.recyclerComplaintStatus) // TextView for complaint status
    }
}
