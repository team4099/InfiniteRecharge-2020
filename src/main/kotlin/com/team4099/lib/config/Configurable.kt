package com.team4099.lib.config

import kotlin.reflect.KMutableProperty0

/**
 * A function to be called when the values of a [Configurable] change.
 */
typealias UpdateHook = () -> Unit

/**
 * Represents an object that can be manipulated through SmartDashboard.
 */
abstract class Configurable<T>(private val fromDouble: (Double) -> T) {
    /**
     * A prefix for the keys used in SmartDashboard. Typically will be the name of the subsystem or other qualifier.
     */
    abstract val keyPrefix: String

    /**
     * A map of SmartDashboard keys to properties that can be changed.
     */
    abstract val properties: Map<String, KMutableProperty0<T>>

    /**
     * The function to be called when the properties change.
     */
    abstract var updateHook: UpdateHook

    fun updateProperty(name: String, value: Double) {
        properties[name]?.set(fromDouble(value)) ?: return
        updateHook()
    }
}
