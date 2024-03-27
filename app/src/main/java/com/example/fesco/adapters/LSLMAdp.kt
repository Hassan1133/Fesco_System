package com.example.fesco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fesco.R
import com.example.fesco.activities.ls.LSLMDetailsActivity
import com.example.fesco.models.LMModel

class LSLMAdp(private val context: Context, private val lmList: List<LMModel>) :
    RecyclerView.Adapter<LSLMAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.xen_sdo_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lmModel = lmList[position]

        // Set data from LMModel to the views in the ViewHolder
        holder.name.text = lmModel.name
        holder.subDivision.text = lmModel.subDivision

        // Set click listener to handle item click events
        holder.itemView.setOnClickListener {
            val intent = Intent(context, LSLMDetailsActivity::class.java)
            intent.putExtra("lmModel", lmModel)
            context.startActivity(intent)
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return lmList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.userName)
        val subDivision: TextView = itemView.findViewById(R.id.subDivision)
    }
}
