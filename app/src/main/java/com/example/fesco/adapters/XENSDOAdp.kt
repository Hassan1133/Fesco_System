package com.example.fesco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fesco.R
import com.example.fesco.activities.xen.XENSDODetailsActivity
import com.example.fesco.models.SDOModel

class XENSDOAdp(private val context: Context, private val sdoList: List<SDOModel>) :
    RecyclerView.Adapter<XENSDOAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.xen_sdo_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds the data to the views in each item of the RecyclerView
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val sdoModel = sdoList[position]

        holder.name.text = sdoModel.name
        holder.subDivision.text = sdoModel.subDivision

        // Handle item click to navigate to SDO details activity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, XENSDODetailsActivity::class.java)
            intent.putExtra("sdoModel", sdoModel)
            context.startActivity(intent)
        }
    }

    // Returns the total number of items in the RecyclerView
    override fun getItemCount(): Int {
        return sdoList.size
    }

    // ViewHolder class holds the views for each item in the RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.userName) // TextView for SDO name
        val subDivision: TextView = itemView.findViewById(R.id.subDivision) // TextView for SDO sub-division
    }
}