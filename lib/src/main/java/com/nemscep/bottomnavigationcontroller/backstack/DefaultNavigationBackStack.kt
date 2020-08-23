/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.backstack

/**
 * Default implementation of [NavigationBackStack].
 */
object DefaultNavigationBackStack : NavigationBackStack<String> {

    private val mBackStack: MutableList<String> = mutableListOf()

    override fun push(value: String) {
        mBackStack.remove(value)
        mBackStack.add(value)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun pop(): String = mBackStack.removeLast()

    override fun peek(): String = mBackStack.last()

    override fun search(value: String): String? = mBackStack.firstOrNull { it == value }

    override fun clear() = mBackStack.clear()

    override fun size() = mBackStack.size

    override fun isEmpty(): Boolean = mBackStack.isEmpty()

    override fun iterable(): Iterator<String> = mBackStack.iterator()
}
