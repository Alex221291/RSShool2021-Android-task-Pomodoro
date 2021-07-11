package com.example.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)