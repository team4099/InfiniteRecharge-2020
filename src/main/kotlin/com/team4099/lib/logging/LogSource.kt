package com.team4099.lib.logging

data class LogSource(val name: String, val supplier: () -> String)
