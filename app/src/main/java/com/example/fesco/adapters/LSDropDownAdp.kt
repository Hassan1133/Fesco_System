package com.example.fesco.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.fesco.R
import com.example.fesco.models.LMModel
import com.example.fesco.models.LSModel

class LSDropDownAdp (
    context: Context,
    private val lmList: List<LSModel> // List of LMModel objects to populate the dropdown
) : ArrayAdapter<LSModel>(context, R.layout.drop_down_item, lmList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.drop_down_item, parent, false)
        }

        val lmModel = getItem(position)

        val textView = view!!.findViewById<TextView>(R.id.lmName)

        textView.text = lmModel?.name

        return view
    }

    override fun getItem(position: Int): LSModel? {
        return lmList[position]
    }

    override fun getCount(): Int {
        return lmList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}