package com.example.wakeup

data class Alarms(
    val hour: Int,
    val minute: Int,
    val days: List<String>,
    val ringtone: String,
    var isEnabled: Boolean = true
)
