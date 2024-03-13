package com.example.fesco.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fesco.R
import com.example.fesco.databinding.FragmentLSComplaintBinding

class LSComplaintFragment : Fragment() {

    private lateinit var binding: FragmentLSComplaintBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLSComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }
}