package com.example.fesco.activities.ls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fesco.databinding.ActivityLslmdetailsBinding
import com.example.fesco.models.LMModel

class LSLMDetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLslmdetailsBinding // Binding for the activity layout

    private lateinit var lmModel : LMModel // Model class to hold LM details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLslmdetailsBinding.inflate(layoutInflater) // Inflate the activity layout
        getDataFromIntent() // Retrieve LM details from the intent
        setContentView(binding.root) // Set the content view
    }

    private fun getDataFromIntent() {
        // Retrieve LMModel object passed through intent
        lmModel = intent.getSerializableExtra("lmModel") as LMModel
        // Set LM details to the corresponding views in the layout
        binding.name.text = lmModel.name
        binding.email.text = lmModel.email
        binding.city.text = lmModel.city
        binding.subDivision.text = lmModel.subDivision
    }
}
