/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.example.sampleapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_two.*

class TwoFragment : Fragment(R.layout.fragment_two) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btn_go_to_twoA.setOnClickListener { findNavController().navigate(R.id.twoAFragment) }
    }
}
