package com.example.fesco.activities.ls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fesco.databinding.ActivityLslmdetailsBinding
import com.example.fesco.models.LMModel

class LSLMDetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLslmdetailsBinding

    private lateinit var lmModel : LMModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLslmdetailsBinding.inflate(layoutInflater)
        getDataFromIntent()
        setContentView(binding.root)
    }

    private fun getDataFromIntent()
    {
        lmModel = intent.getSerializableExtra("lmModel") as LMModel
        binding.name.text = lmModel.name
        binding.email.text = lmModel.email
        binding.city.text = lmModel.city
        binding.subDivision.text = lmModel.subDivision
    }
}