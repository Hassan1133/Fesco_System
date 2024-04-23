package com.example.fesco.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.fesco.R
import com.example.fesco.models.SDOModel

class SDODropDownAdp (
    context: Context,
    private val sdoList: List<SDOModel> // List of LMModel objects to populate the dropdown
) : ArrayAdapter<SDOModel>(context, R.layout.drop_down_item, sdoList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.drop_down_item, parent, false)
        }

        val lmModel = getItem(position)

        val textView = view!!.findViewById<TextView>(R.id.lmName)

        textView.text = lmModel.name

        return view
    }

    override fun getItem(position: Int): SDOModel {
        return sdoList[position]
    }

    override fun getCount(): Int {
        return sdoList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}