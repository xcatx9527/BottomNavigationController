/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.controller

import androidx.lifecycle.LiveData
import androidx.navigation.NavController

interface BottomNavigationController {
    val currentNavController: LiveData<NavController>
    fun onBackPressed(activityOnBackPressed: () -> Unit)
}
