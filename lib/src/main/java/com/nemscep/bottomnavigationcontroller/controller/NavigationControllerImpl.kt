/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.controller

import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.iterator
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nemscep.bottomnavigationcontroller.backstack.*

class BottomNavigationControllerImpl private constructor(
    private val mBottomNavigationView: BottomNavigationView,
    mActivity: AppCompatActivity,
    mContainerView: View,
    mGraphIdsList: List<Int>
) : BottomNavigationController {

    private val mFragmentManager = mActivity.supportFragmentManager
    private val mBackStack: NavigationBackStack = NavigationBackStack()

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
        val graphIdToTag = SparseArray<String>()
        val selectedNavController = MutableLiveData<NavController>()
        var firstFragmentGraphId = 0

        mGraphIdsList.forEachIndexed { index, graphId ->
            val tag = getFragmentTag(index)
            val navHostFragment =
                obtainNavHostFragment(mFragmentManager, tag, graphId, mContainerView.id)

            val gId = navHostFragment.navController.graph.id
            if (index == 0) {
                firstFragmentGraphId = gId
            }
            graphIdToTag[gId] = tag

            if (mBottomNavigationView.selectedItemId == gId) {
                // Update livedata with the selected graph
                selectedNavController.value = navHostFragment.navController
                attachNavHostFragment(mFragmentManager, navHostFragment, mBackStack)
            } else {
                detachNavHostFragment(mFragmentManager, navHostFragment)
            }
        }

        val firstFragmentTag = graphIdToTag[firstFragmentGraphId]
        mBackStack.push(firstFragmentTag)

        mBottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (mFragmentManager.isStateSaved) return@setOnNavigationItemSelectedListener false

            val newlySelectedItemTag = graphIdToTag[item.itemId]
            val selectedFragment =
                mFragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment
            // push currently selected tag to backstack
            mBackStack.push(newlySelectedItemTag)

            mFragmentManager.beginTransaction()
                .attach(selectedFragment)
                .setPrimaryNavigationFragment(selectedFragment)
                .apply {
                    // Detach all other Fragments
                    graphIdToTag.forEach { _, fragmentTagIter ->
                        if (fragmentTagIter != newlySelectedItemTag) {
                            detach(
                                mFragmentManager.findFragmentByTag(fragmentTagIter)!!
                            )
                        }
                    }
                }
                .setReorderingAllowed(true)
                .commit()

            selectedNavController.value = selectedFragment.navController
            true
        }

        mBottomNavigationView.setOnNavigationItemReselectedListener { item ->
            val newlySelectedItemTag = graphIdToTag[item.itemId]
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

        fun bindFragmentManager(fragmentManager: FragmentManager) =
            apply { this.fragmentManager = fragmentManager }

        fun bindFragmentContainerView(fragmentContainerView: FragmentContainerView) =
            apply { this.fragmentContainerView = fragmentContainerView }

        fun bindActivity(activity: AppCompatActivity) = apply { this.activity = activity }

        fun build() = BottomNavigationControllerImpl(
            bottomNavigationView,
            activity,
            fragmentContainerView,
            graphIds
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
