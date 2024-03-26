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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.xen_sdo_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val sdoModel = sdoList[position]

        holder.name.text = sdoModel.name
        holder.subDivision.text = sdoModel.subDivision

        holder.itemView.setOnClickListener {
            val intent = Intent(context, XENSDODetailsActivity::class.java)
            intent.putExtra("sdoModel", sdoModel)
            context.startActivity(intent)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return sdoList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.userName)
        val subDivision: TextView = itemView.findViewById(R.id.subDivision)
    }
}