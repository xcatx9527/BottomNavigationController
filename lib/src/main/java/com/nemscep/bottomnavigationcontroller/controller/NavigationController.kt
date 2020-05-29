/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.controller

import androidx.lifecycle.LiveData
import androidx.navigation.NavController

interface BottomNavigationController {
    /**
     * Live data for current nav controller
     */
    val currentNavController: LiveData<NavController>
}
