/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.example.sampleapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.nemscep.bottomnavigationcontroller.controller.BottomNavigationController
import com.nemscep.bottomnavigationcontroller.controller.BottomNavigationControllerImpl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var navigationController: BottomNavigationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigationController = BottomNavigationControllerImpl.Builder()
                .bindActivity(this)
                .bindBottomNavigation(bnb_example)
                .bindFragmentContainerView(fcv_main)
                .bindNavGraphs(
                    R.navigation.one_graph,
                    R.navigation.two_graph,
                    R.navigation.three_graph,
                    R.navigation.four_graph
                ).build()

            navigationController.currentNavController.observe(this, Observer {
                setupActionBarWithNavController(it)
            })
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        navigationController = BottomNavigationControllerImpl.Builder()
            .bindActivity(this)
            .bindBottomNavigation(bnb_example)
            .bindFragmentContainerView(fcv_main)
            .bindNavGraphs(
                R.navigation.one_graph,
                R.navigation.two_graph,
                R.navigation.three_graph,
                R.navigation.four_graph
            ).build()

        navigationController.currentNavController.observe(this, Observer {
            setupActionBarWithNavController(it)
        })
    }

    override fun onBackPressed() {
        if (!navigationController.onBackPressed()) super.onBackPressed()
    }

}
