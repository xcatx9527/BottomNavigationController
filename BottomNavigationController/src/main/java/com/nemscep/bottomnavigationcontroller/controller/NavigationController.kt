package com.nemscep.bottomnavigationcontroller.controller

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


interface BottomNavigationController {
    val currentNavController: LiveData<NavController>
    fun onBackPressed(activityOnBackPressed: () -> Unit)
}

class NavigationController(graphIds: List<Int>) : BottomNavigationController {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var mNavHostHolder: NavHostHolder
    private lateinit var activity: AppCompatActivity

    private val mBackStack = NavigationBackStack()

    private val _currentNavController = MutableLiveData<NavController>()
    override val currentNavController =
        _currentNavController.distinctUntilChanged() as LiveData<NavController>

    private val navHostFragments: List<NavHostFragment> =
        graphIds.map { NavHostFragment.create(it) }.also { navHostFragments = it }

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
        bottomNavigationView.menu.iterator()
            .withIndex()
            .asSequence()
            .toList().find { it.value.itemId == menuItemId }?.index
            ?: throw IllegalStateException("Shouldn't be here")

    private fun attachBottomNavigationListeners() {
        bottomNavigationView.apply {

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
            adapter = NavHostHolderAdapter(activity.supportFragmentManager, navHostFragments)
            val listener = object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) = Unit

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) = Unit

                override fun onPageSelected(position: Int) {
                    val fragment = navHostFragments[position]
                    if (bottomNavigationView.selectedItemId != fragment.id) {
                        bottomNavigationView.selectedItemId =
                            bottomNavigationView.menuItemList()[position].value.itemId
                        _currentNavController.postValue(fragment.findNavController())
                    }
                }
            }

            addOnPageChangeListener(listener)
            post(Runnable { listener.onPageSelected(mBackStack.peek()) })
        }
    }


    fun bindActivity(activity: AppCompatActivity) = apply { this.activity = activity }.also {
        activity.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {

                if (mBackStack.isEmpty()) {
                    mBackStack.push(bottomNavigationView.menuItemList().first().index)
                }

                attachBottomNavigationListeners()
                attachViewPagerListeners()
            }

        })
    }

    fun bindBottomNavigation(bottomNavigationView: BottomNavigationView) =
        apply { this.bottomNavigationView = bottomNavigationView }

    fun bindContainerView(navHostHolder: NavHostHolder) =
        apply { this.mNavHostHolder = navHostHolder }

}

fun BottomNavigationView.menuItemList() =
    menu.iterator().withIndex().asSequence().toList()
