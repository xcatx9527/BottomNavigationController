/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.util

import androidx.core.view.iterator
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Returns menuItem list from [BottomNavigationView] object.
 */
fun BottomNavigationView.menuItemList() =
    menu.iterator().withIndex().asSequence().toList()
