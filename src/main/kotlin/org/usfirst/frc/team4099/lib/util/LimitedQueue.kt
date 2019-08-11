package org.usfirst.frc.team4099.lib.util

import java.util.*

class LimitedQueue<E>(private val limit: Int) : LinkedList<E>() {

    override fun add(o: E): Boolean {
        super.add(o)
        while (size > limit) {
            super.remove()
        }
        return true
    }
}