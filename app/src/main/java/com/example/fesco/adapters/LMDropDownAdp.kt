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
    private val lmList: List<LMModel>,
    private val listener: OnDropDownItemClickListener
) : ArrayAdapter<LMModel>(context, R.layout.drop_down_item, lmList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.drop_down_item, parent, false)
        }

        val lmModel = getItem(position)

        val textView = view!!.findViewById<TextView>(R.id.lmName)
        textView.text = lmModel?.name

        textView.setOnClickListener {
            lmModel?.let { listener.onItemClick(it.id, it.name) }
        }

        return view
    }
}