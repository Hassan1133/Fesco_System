package com.example.fesco.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.fesco.R
import com.example.fesco.interfaces.OnDropDownItemClickListener
import com.example.fesco.models.LMModel

class LMDropDownAdp(
    context: Context,
    private val lmList: List<LMModel>, // List of LMModel objects to populate the dropdown
    private val listener: OnDropDownItemClickListener // Listener interface for item clicks
) : ArrayAdapter<LMModel>(context, R.layout.drop_down_item, lmList) {

    // Override getView method to customize the appearance of each dropdown item
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            // Inflate layout for dropdown item if convertView is null
            view = LayoutInflater.from(context).inflate(R.layout.drop_down_item, parent, false)
        }

        // Get LMModel object at the specified position
        val lmModel = getItem(position)

        // Find TextView in the dropdown item layout
        val textView = view!!.findViewById<TextView>(R.id.lmName)

        // Set text of the TextView to the name of the LMModel object
        textView.text = lmModel?.name

        // Set click listener for the TextView to handle item clicks
        textView.setOnClickListener {
            lmModel?.let { listener.onItemClick(it.id, it.name) } // Pass item ID and name to the listener
        }

        return view
    }
}