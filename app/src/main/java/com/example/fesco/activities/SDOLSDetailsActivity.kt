package com.example.fesco.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fesco.R
import com.example.fesco.databinding.ActivitySdolsdetailsBinding
import com.example.fesco.databinding.ActivityXensdodetailsBinding
import com.example.fesco.models.LSModel
import com.example.fesco.models.SDOModel

class SDOLSDetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySdolsdetailsBinding

    private lateinit var lsModel : LSModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdolsdetailsBinding.inflate(layoutInflater)
        getDataFromIntent()
        setContentView(binding.root)
    }

    private fun getDataFromIntent()
    {
        lsModel = intent.getSerializableExtra("lsModel") as LSModel
        binding.name.text = lsModel.name
        binding.email.text = lsModel.email
        binding.city.text = lsModel.city
        binding.subDivision.text = lsModel.subDivision
    }
}