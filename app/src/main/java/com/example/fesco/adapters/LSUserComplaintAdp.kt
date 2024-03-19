package com.example.fesco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fesco.R
import com.example.fesco.activities.LSUserComplaintDetailsActivity
import com.example.fesco.models.UserComplaintModel

class LSUserComplaintAdp (private val context: Context, private val complaintList: List<UserComplaintModel>) :
    RecyclerView.Adapter<LSUserComplaintAdp.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_complaint_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val userComplaintModel = complaintList[position]

        holder.complaintType.text = userComplaintModel.complaintType
        holder.complaintDateTime.text = userComplaintModel.dateTime
        holder.complaintStatus.text = userComplaintModel.status

        holder.itemView.setOnClickListener {
            val intent = Intent(context, LSUserComplaintDetailsActivity::class.java)
            intent.putExtra("userComplaintModel", userComplaintModel)
            context.startActivity(intent)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return complaintList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val complaintType: TextView = itemView.findViewById(R.id.recyclerComplaintType)
        val complaintDateTime: TextView = itemView.findViewById(R.id.recyclerComplaintDateTime)
        val complaintStatus: TextView = itemView.findViewById(R.id.recyclerComplaintStatus)
    }
}