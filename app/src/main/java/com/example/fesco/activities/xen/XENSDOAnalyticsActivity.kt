package com.example.fesco.activities.xen

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fesco.R
import com.example.fesco.databinding.ActivityXensdoanalyticsBinding
import com.example.fesco.models.UserComplaintModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore

class XENSDOAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityXensdoanalyticsBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var updatedComplaintList: MutableList<UserComplaintModel>
    private var sdoId = ""
    private var status = ""
    private var date = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXensdoanalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        firestoreDb = FirebaseFirestore.getInstance()
        updatedComplaintList = mutableListOf<UserComplaintModel>()
        getDataFromIntent()
    }

    private fun getDataFromIntent() {
        sdoId = intent.getStringExtra("sdoId")!!
        status = intent.getStringExtra("status")!!
        date = intent.getStringExtra("date")!!

        getSDOUserComplaintsID()
    }

    private fun getSDOUserComplaintsID() {
        firestoreDb.collection("SDO").document(sdoId)
            .addSnapshotListener { snapShot, exception ->
                if (exception != null) {

                    return@addSnapshotListener
                }
                snapShot?.let { document ->
                    val complaints = document.get("complaints") as? List<String>
                    complaints?.let {
                        // Fetch complaint details from Firestore
                        getSDOUserComplaintDataFromDb(it)
                    } ?: run {

                    }
                }
            }
    }

    // Fetch details of LS user complaints from Firestore
    private fun getSDOUserComplaintDataFromDb(complaintList: List<String>) {
        if (complaintList.isEmpty()) {

            return
        }
        firestoreDb.collection("UserComplaints").whereIn("id", complaintList)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {

                    return@addSnapshotListener
                }
                updatedComplaintList.clear()
                snapshots?.documents?.forEach { documentSnapshot ->
                    val complaint = documentSnapshot.toObject(UserComplaintModel::class.java)
                    complaint?.let {
                        if (complaint.dateTime.contains(date)) {
                            if (complaint.status == status) {
                                // Add unresolved complaints to the list
                                updatedComplaintList.add(it)
                            } else if (status == "All") {
                                updatedComplaintList.add(it)
                            }
                        }

                    }
                }
                getFilteredList(updatedComplaintList)
            }
    }

    private fun getFilteredList(updatedComplaintList: MutableList<UserComplaintModel>) {
        if (updatedComplaintList.size > 0) {
            val entries = mutableListOf<BarEntry>()
            val complainLabelList = ArrayList<String>()
            for (i in updatedComplaintList) {
                if (!complainLabelList.contains(i.complaintType)) {
                    complainLabelList.add(i.complaintType)
                }
            }

            for (j in 0..<complainLabelList.size) {
                var count = 0
                for (i in updatedComplaintList) {
                    if (i.complaintType == complainLabelList[j]) {
                        count++
                    }
                }
                entries.add(BarEntry(j.toFloat(), count.toFloat()))
            }

            setBarChart(complainLabelList, entries)
        }

    }

    private fun setBarChart(complainLabelList: ArrayList<String>, entries: MutableList<BarEntry>) {
        val barChart: BarChart = findViewById(R.id.barChart)
        barChart.axisRight.setDrawLabels(false)


        val yAxis = barChart.axisLeft
        yAxis.axisLineWidth = 2f
        yAxis.axisLineColor = Color.BLACK

        val dataSet = BarDataSet(entries, "Complaint Types")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toMutableList()
        dataSet.valueTextSize = 13f

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.description.isEnabled = false
        barChart.invalidate()

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(complainLabelList)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true
        val xAxis = barChart.xAxis
        xAxis.textSize = 6f

        barChart.animateY(5000)
    }
}