/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.controller

import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.iterator
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nemscep.bottomnavigationcontroller.backstack.*

class BottomNavigationControllerImpl private constructor(
    private val mBottomNavigationView: BottomNavigationView,
    mActivity: AppCompatActivity,
    mContainerView: View,
    mGraphIdsList: List<Int>,
    savedInstanceState: Bundle?
) : BottomNavigationController {

    private val mFragmentManager = mActivity.supportFragmentManager
    private val mBackStack: NavigationBackStack = NavigationBackStack()

    private val fragmentDestinationIdMap = SparseArray<String>()

    private val _currentNavController = MutableLiveData<NavController>()
    override val currentNavController =
        _currentNavController.distinctUntilChanged() as LiveData<NavController>

    override fun onBackPressed(activityOnBackPressed: () -> Unit) {
        val currentFragment =
            mFragmentManager.findFragmentByTag(mBackStack.peek()) as NavHostFragment
        if (!currentFragment.navController.navigateUp()) {
            if (mBackStack.size() > 1) {
                // remove current position from stack
                mBackStack.pop()
                val newTop = mFragmentManager.findFragmentByTag(mBackStack.peek())!!
                // set the next item in stack as current
                mBottomNavigationView.selectedItemId = newTop.findNavController().graph.id
            } else activityOnBackPressed.invoke()
        }
    }

    init {

        mGraphIdsList.forEachIndexed { index, graphId ->
            val tag = getFragmentTag(index)
            val navHostFragment =
                obtainNavHostFragment(mFragmentManager, tag, graphId, mContainerView.id)

            val fragmentDestinationId = navHostFragment.navController.graph.id
            fragmentDestinationIdMap[fragmentDestinationId] = tag

            if (mBottomNavigationView.selectedItemId == fragmentDestinationId) {
                // push BottomNavigationView's current item to stack
                mBackStack.push(navHostFragment.tag!!)
                // Update livedata with the selected graph
                _currentNavController.value = navHostFragment.navController
                attachNavHostFragment(mFragmentManager, navHostFragment, mBackStack)
            } else {
                detachNavHostFragment(mFragmentManager, navHostFragment)
            }
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (mFragmentManager.isStateSaved) return@setOnNavigationItemSelectedListener false

            val newlySelectedItemTag = fragmentDestinationIdMap[item.itemId]
            val selectedFragment =
                mFragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment
            // push currently selected tag to backstack
            mBackStack.push(newlySelectedItemTag)

            // attach currently selected fragment and detach others
            mFragmentManager.beginTransaction()
                .attach(selectedFragment)
                .setPrimaryNavigationFragment(selectedFragment)
                .apply {
                    // Detach all other Fragments
                    fragmentDestinationIdMap.forEach { _, tag ->
                        if (tag != newlySelectedItemTag) {
                            detach(
                                mFragmentManager.findFragmentByTag(tag)!!
                            )
                        }
                    }
                }
                .setReorderingAllowed(true)
                .commit()

            _currentNavController.value = selectedFragment.navController
            return@setOnNavigationItemSelectedListener true
        }

        mBottomNavigationView.setOnNavigationItemReselectedListener { item ->
            val newlySelectedItemTag = fragmentDestinationIdMap[item.itemId]
            val selectedFragment = mFragmentManager.findFragmentByTag(newlySelectedItemTag)
                    as NavHostFragment
            val navController = selectedFragment.navController
            // Pop the back stack to the start destination of the current navController graph
            navController.popBackStack(navController.graph.startDestination, false)
        }

    }

    object Builder {
        private lateinit var bottomNavigationView: BottomNavigationView
        private lateinit var activity: AppCompatActivity
        private lateinit var fragmentManager: FragmentManager
        private lateinit var graphIds: List<Int>
        private lateinit var fragmentContainerView: FragmentContainerView
        private var savedInstanceState: Bundle? = null

        fun bindBottomNavigation(bottomNavigationView: BottomNavigationView) =
            apply { this.bottomNavigationView = bottomNavigationView }

        fun bindNavGraphs(vararg graphId: Int) = apply {
            val graphsList = mutableListOf<Int>()
            for (id in graphId) {
                graphsList.add(id)
            }
            if (graphsList.size != bottomNavigationView.menuItemList().size) {
                throw java.lang.IllegalStateException("Graph ids list passed isn't size compatible with menu items ")
            }
            graphIds = graphsList
        }

        fun bindSavedInstanceState(bundle: Bundle) = apply {
            this.savedInstanceState = bundle
        }

        fun bindFragmentManager(fragmentManager: FragmentManager) =
            apply { this.fragmentManager = fragmentManager }

        fun bindFragmentContainerView(fragmentContainerView: FragmentContainerView) =
            apply { this.fragmentContainerView = fragmentContainerView }

        fun bindActivity(activity: AppCompatActivity) = apply { this.activity = activity }

        fun build() = BottomNavigationControllerImpl(
            bottomNavigationView,
            activity,
            fragmentContainerView,
            graphIds,
            savedInstanceState
        )
    }

}

class SingleInstance<T>(lambda: () -> T) {
    private val elem by lazy { lambda.invoke() }
    fun get() = elem
}

fun <T> singleInstance(labmda: () -> T) = SingleInstance(labmda).get()

fun BottomNavigationView.menuItemList() =
    menu.iterator().withIndex().asSequence().toList()

fun BottomNavigationView.getItemIndexForMenuItemId(menuItemId: Int) =
    menu.iterator()
        .withIndex()
        .asSequence()
        .toList().find { it.value.itemId == menuItemId }?.index
        ?: throw IllegalStateException("Shouldn't be here")
