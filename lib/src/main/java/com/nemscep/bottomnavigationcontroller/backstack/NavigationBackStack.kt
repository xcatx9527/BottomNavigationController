/*
 * Copyright (c) 2020. Created by Nemanja Scepanovic
 */

package com.nemscep.bottomnavigationcontroller.backstack

interface NavigationBackStack<T> {
    /**
     * Pushes given value to the stack.
     * If the value already exists, it removes it and adds it on top of stack
     */
    fun push(value: T)

    /**
     * Gets topmost element on the stack and removing it from it
     */
    fun pop(): T

    /**
     * Gets the topmost element on the stack without removing it from it
     */
    fun peek(): T

    /**
     * Tries to find given element in the stack, if it doesn't exist returns null
     */
    fun search(value: T): T?

    /**
     * Clears the entire backstack
     */
    fun clear()

    /**
     * Returns the current size of backstack
     */
    fun size(): Int

    /**
     * Returns whether backstack is empty or not
     */
    fun isEmpty(): Boolean

    /**
     * Returns iterable to help
     */
    fun iterable(): Iterator<T>
}
