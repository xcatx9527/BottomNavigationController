/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.backstack

import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment

/**
 * Detaches given nav host fragment
 */
fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) = fragmentManager.beginTransaction()
    .detach(navHostFragment)
    .commitNow()

/**
 * Attaches given nav host fragment
 */
fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    mBackStack: NavigationBackStack
) = fragmentManager.beginTransaction()
    .attach(navHostFragment)
    .setPrimaryNavigationFragment(navHostFragment)
    .also {
        mBackStack.iterable().forEach {
            if (navHostFragment.tag != it) {
                detachNavHostFragment(
                    fragmentManager,
                    fragmentManager.findFragmentByTag(it)!! as NavHostFragment
                )
            }
        }
    }
    .commitNow()

/**
 * Returns nav host fragment if it exists in backstack
 */
fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction().add(containerId, navHostFragment, fragmentTag).commitNow()
    return navHostFragment
}

fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

fun getFragmentTag(index: Int) = "bottomNavigation#$index"

