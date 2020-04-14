/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.controller

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nemscep.bottomnavigationcontroller.backstack.NavigationBackStack
import com.nemscep.bottomnavigationcontroller.ui.NavHostHolder
import com.nemscep.bottomnavigationcontroller.ui.NavHostHolderAdapter

class BottomNavigationControllerImpl private constructor() : BottomNavigationController {

    private lateinit var mBottomNavigationView: BottomNavigationView
    private lateinit var mNavHostHolder: NavHostHolder
    private lateinit var mActivity: AppCompatActivity // support fragment manager
    private lateinit var navHostFragments: List<NavHostFragment>

    private val mBackStack: NavigationBackStack = NavigationBackStack()

    private val _currentNavController = MutableLiveData<NavController>()
    override val currentNavController =
        _currentNavController.distinctUntilChanged() as LiveData<NavController>


    override fun onBackPressed(activityOnBackPressed: () -> Unit) {
        val navController = navHostFragments[mNavHostHolder.currentItem].findNavController()
        if (!navController.navigateUp()) {
            if (mBackStack.size() > 1) {
                // remove current position from stack
                mBackStack.pop()
                // set the next item in stack as current
                mNavHostHolder.currentItem = mBackStack.peek()

            } else activityOnBackPressed.invoke()
        }
    }

    private fun getItemIndexForMenuItemId(menuItemId: Int) =
        mBottomNavigationView.menu.iterator()
            .withIndex()
            .asSequence()
            .toList().find { it.value.itemId == menuItemId }?.index
            ?: throw IllegalStateException("Shouldn't be here")

    private fun attachBottomNavigationListeners() {
        mBottomNavigationView.apply {

            setOnNavigationItemReselectedListener {
                val navController =
                    navHostFragments[mNavHostHolder.currentItem].findNavController()
                if (navController.currentDestination?.id != navController.graph.startDestination) {
                    navController.popBackStack()
                }
            }

            setOnNavigationItemSelectedListener { menuItem ->
                val pressedMenuItemIndex = getItemIndexForMenuItemId(menuItem.itemId)
                if (mNavHostHolder.currentItem != pressedMenuItemIndex) {
                    mNavHostHolder.currentItem = pressedMenuItemIndex
                    mBackStack.push(pressedMenuItemIndex)
                }
                true
            }

        }

    }

    private fun attachViewPagerListeners() {
        mNavHostHolder.apply {
            adapter = NavHostHolderAdapter(mActivity.supportFragmentManager, navHostFragments)
            val listener = object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) = Unit

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) = Unit

                override fun onPageSelected(position: Int) {
                    val fragment = navHostFragments[position]
                    if (mBottomNavigationView.selectedItemId != fragment.id) {
                        mBottomNavigationView.selectedItemId =
                            mBottomNavigationView.menuItemList()[position].value.itemId
                        _currentNavController.postValue(fragment.findNavController())
                    }
                }
            }

            addOnPageChangeListener(listener)
            post { listener.onPageSelected(mBackStack.peek()) }
        }
    }

    class Builder {
        private lateinit var bottomNavigationView: BottomNavigationView
        private lateinit var navHostHolder: NavHostHolder
        private lateinit var activity: AppCompatActivity
        private lateinit var graphIds: List<Int>

        // singleton implementation
        private val navigationControllerImpl by lazy { BottomNavigationControllerImpl() }

        fun bindBottomNavigation(bottomNavigationView: BottomNavigationView) =
            apply { this.bottomNavigationView = bottomNavigationView }

        fun bindContainerView(navHostHolder: NavHostHolder) =
            apply { this.navHostHolder = navHostHolder }

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


        fun bindActivity(activity: AppCompatActivity) =
            apply { this.activity = activity }.also {

            }

        fun build() = navigationControllerImpl.apply {
            this.mBottomNavigationView = bottomNavigationView
            this.mActivity = activity.apply {
                lifecycle.addObserver(object : LifecycleObserver {

                    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                    fun onCreate() {

                        if (mBackStack.isEmpty()) {
                            mBackStack.push(0)
                        }

                        attachBottomNavigationListeners()
                        attachViewPagerListeners()
                    }
                })
            }
            this.navHostFragments = graphIds.map { NavHostFragment.create(it) }
            this.mNavHostHolder = navHostHolder
        }
    }

}

fun BottomNavigationView.menuItemList() =
    menu.iterator().withIndex().asSequence().toList()
