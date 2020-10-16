/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.example.sampleapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.ui.setupActionBarWithNavController
import com.nemscep.bottomnavigationcontroller.controller.BottomNavigationController
import com.nemscep.bottomnavigationcontroller.controller.BottomNavigationControllerImpl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var navigationController: BottomNavigationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)

        if (savedInstanceState == null) {
            navigationController = BottomNavigationControllerImpl.Builder()
                .bindActivity(this)
                .bindBottomNavigationView(bnb_example)
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
            .bindBottomNavigationView(bnb_example)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
