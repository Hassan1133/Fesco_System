package com.example.fesco.activities.xen

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.databinding.ActivityXensdodetailsBinding
import com.example.fesco.models.SDOModel
import com.google.android.gms.common.internal.safeparcel.SafeParcelable

class XENSDODetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityXensdodetailsBinding

    private lateinit var sdoModel : SDOModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXensdodetailsBinding.inflate(layoutInflater)
        getDataFromIntent()
        setContentView(binding.root)
    }

    private fun getDataFromIntent()
    {
        sdoModel = intent.getSerializableExtra("sdoModel") as SDOModel
        binding.name.text = sdoModel.name
        binding.email.text = sdoModel.email
        binding.city.text = sdoModel.city
        binding.subDivision.text = sdoModel.subDivision
    }
}