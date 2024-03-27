package com.example.fesco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fesco.R
import com.example.fesco.activities.sdo.SDOLSDetailsActivity
import com.example.fesco.models.LSModel

class SDOLSAdp(private val context: Context, private val lsList: List<LSModel>) :
    RecyclerView.Adapter<SDOLSAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.xen_sdo_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lsModel = lsList[position]

        // Set LSModel data to the views in the ViewHolder
        holder.name.text = lsModel.name
        holder.subDivision.text = lsModel.subDivision

        // Set click listener to handle item click events
        holder.itemView.setOnClickListener {
            val intent = Intent(context, SDOLSDetailsActivity::class.java)
            intent.putExtra("lsModel", lsModel)
            context.startActivity(intent)
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return lsList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.userName)
        val subDivision: TextView = itemView.findViewById(R.id.subDivision)
    }
}
