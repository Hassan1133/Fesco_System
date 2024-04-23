package com.example.fesco.fragments.xen

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fesco.R
import com.example.fesco.activities.xen.XENSDOAnalyticsActivity
import com.example.fesco.adapters.SDODropDownAdp
import com.example.fesco.databinding.FragmentXENAnalyticsBinding
import com.example.fesco.models.SDOModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class XENAnalyticsFragment : Fragment() {

    private lateinit var binding: FragmentXENAnalyticsBinding

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var lsList: List<SDOModel>
    private var sdoId = ""
    private var selectedStatus = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXENAnalyticsBinding.inflate(inflater, container, false)
        init()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun init() {
        lsList = mutableListOf()
        firestoreDb = Firebase.firestore // Initialize Firestore instance
        getSDOArrayFromSharedPreferences()
        setupStatusAutoCompleteTextView()
        setupCalenderAutoCompleteTextView()

        binding.seeAnalyticsBtn.setOnClickListener {
            if (isValid()) {
                val intent = Intent(requireActivity(), XENSDOAnalyticsActivity::class.java)
                intent.putExtra("sdoId", sdoId)
                intent.putExtra("status", selectedStatus)
                intent.putExtra("date", binding.date.text.toString())
                startActivity(intent)
            }
        }
    }


    private fun showMonthPicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val monthPickerValues = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        // Inflate the custom layout
        val view = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker).apply {
            minValue = 0
            maxValue = 11
            displayedValues = monthPickerValues
            value = currentMonth
        }

        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker).apply {
            minValue = currentYear - 100 // Set min year to 100 years before current year
            maxValue = currentYear + 100 // Set max year to 100 years after current year
            value = currentYear
        }

        // Create the AlertDialog
        AlertDialog.Builder(requireActivity())
            .setTitle("Select Month and Year")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val selectedMonth = monthPicker.value
                val selectedYear = yearPicker.value
                val formattedDate = "${monthPickerValues[selectedMonth]} $selectedYear"
                binding.date.setText(formattedDate)

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showYearPicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // Create a NumberPicker for selecting the year
        val yearPicker = NumberPicker(requireActivity()).apply {
            minValue = currentYear - 100 // Set min year to 100 years before current year
            maxValue = currentYear + 100 // Set max year to 100 years after current year
            value = currentYear // Set initial value to current year
        }

        // Create an AlertDialog with the year picker
        AlertDialog.Builder(requireActivity())
            .setTitle("Select Year")
            .setView(yearPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = yearPicker.value
                binding.date.setText(selectedYear.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.date.setText(dateFormat.format(cal.time))
        }
        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            datePickerListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupStatusAutoCompleteTextView() {
        // Retrieve status options from resources
        val statusArray = resources.getStringArray(R.array.ls_all_status_array)
        // Create ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_dropdown_item_1line, statusArray
        )
        // Set adapter to AutoCompleteTextView
        binding.status.setAdapter(adapter)

        // Set item click listener
        binding.status.setOnItemClickListener { parent, _, position, _ ->
            selectedStatus = parent.getItemAtPosition(position).toString()

        }
    }

    private fun setupCalenderAutoCompleteTextView() {
        // Retrieve status options from resources
        val calenderArray = resources.getStringArray(R.array.calender_type)
        // Create ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_dropdown_item_1line, calenderArray
        )
        // Set adapter to AutoCompleteTextView
        binding.calender.setAdapter(adapter)

        // Set item click listener
        binding.calender.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            when (selectedItem) {
                "Date" -> showDatePicker()
                "Month" -> showMonthPicker()
                "Year" -> showYearPicker()
            }
        }
    }

    private fun getSDOArrayFromSharedPreferences() {
        val sdoArrayString =
            requireActivity().getSharedPreferences("xenData", AppCompatActivity.MODE_PRIVATE)
                ?.getString(
                    "sdo", null
                ) // Get LM data as JSON string
        val sdoArray = sdoArrayString?.let {
            Gson().fromJson(
                it, Array<String>::class.java
            )
        } // Convert JSON to array

        // Check if LM array is retrieved successfully
        sdoArray?.let { getSDODataFromDb(it) } // Get LM data from Firestore using array
    }

    private fun getSDODataFromDb(sdoArray: Array<String>) {
        lifecycleScope.launch { // Use coroutine for asynchronous operations
            lsList = sdoArray.mapNotNull { sdoID -> // Map LM IDs to LM objects
                try {
                    firestoreDb.collection("SDO").document(sdoID).get().await()
                        .toObject(SDOModel::class.java)
                } catch (e: Exception) {

                    null
                }
            }
            getLMNameFromLmList(lsList) // Get LM names from retrieved data
        }
    }

    private fun getLMNameFromLmList(list: List<SDOModel>) {
        val adapter = SDODropDownAdp( // Create adapter for LM dropdown
            requireActivity(), list
        )

        binding.selectSDO.setAdapter(adapter) // Set adapter to dropdown

        binding.selectSDO.setOnItemClickListener { _, _, position, _ ->
            val sdoModel = adapter.getItem(position)
            sdoModel.let {
                binding.selectSDO.setText(it.name)
                sdoId = it.id
            }
        }
    }

    private fun isValid(): Boolean {
        var valid = true

        if (sdoId.isEmpty()) {
            binding.selectSDO.error = "select LM please"
            valid = false
        }
        if (selectedStatus.isEmpty()) {
            binding.status.error = "select status please"
            valid = false
        }
        if (binding.date.text.isNullOrEmpty()) {
            binding.date.error = "select date from calender please"
            valid = false
        }

        return valid
    }
}