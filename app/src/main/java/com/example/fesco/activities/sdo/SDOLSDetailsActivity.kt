package com.example.fesco.activities.sdo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fesco.databinding.ActivitySdolsdetailsBinding
import com.example.fesco.models.LSModel

class SDOLSDetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySdolsdetailsBinding

    private lateinit var lsModel : LSModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdolsdetailsBinding.inflate(layoutInflater)
        try {
            getDataFromIntent() // Call a function to retrieve data from Intent
            setContentView(binding.root) // Set the content view to the root layout of the binding
        } catch (e: Exception) {
            // Handle any exception that occurs during data retrieval
            e.printStackTrace() // Log the exception
            // Optionally, show an error message to the user or navigate back
        }
    }

    // Function to retrieve data from Intent and populate UI elements
    private fun getDataFromIntent() {
        // Retrieve LSModel object from Intent extras
        lsModel = intent.getSerializableExtra("lsModel") as LSModel

        // Populate UI elements with data from LSModel object
        binding.name.text = lsModel.name
        binding.email.text = lsModel.email
        binding.city.text = lsModel.city
        binding.subDivision.text = lsModel.subDivision
    }
}