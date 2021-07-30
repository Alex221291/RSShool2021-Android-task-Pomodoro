package com.example.pomodoro

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import android.widget.Toast
import androidx.annotation.RequiresApi

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

//import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var startedId = -1
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            try {
                time = binding.minutesNewTimer.text.toString().toLong()
                if (time != 0L) {
                    stopwatches.add(
                        Stopwatch(
                            nextId++,
                            time * 1000L * 60L,
                            time * 1000L * 60L,
                            false
                        )
                    )
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else Toast.makeText(baseContext, "Invalid data", Toast.LENGTH_LONG).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(baseContext, "Invalid data", Toast.LENGTH_LONG).show()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun start(id: Int) {
        startedId= id
        changeStopwatch(id, stopwatches.find { it.id == startedId }?.currentMs, true) //stopwatches.find { it.id == startedId }?.currentMs
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun stop(id: Int, currentMs: Long) {
        if (id == startedId) startedId = -1
        changeStopwatch(id, currentMs, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun delete(id: Int) {
        if (id == startedId) startedId = -1
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            when {
                it.id == id -> {
                    newTimers.add(Stopwatch(it.id, it.startMs, it.currentMs, isStarted))
                }
                it.isStarted -> {
                    newTimers.add(Stopwatch(it.id, it.startMs, it.currentMs, false))
                }
                else -> {
                    newTimers.add(it)
                }
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    private var backPressed: Long = 0

    override fun onBackPressed() {
        startedId = -1
        if (backPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(baseContext, "Press once again to exit!", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if(startedId != -1) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(
                STARTED_TIMER_TIME_MS,
                stopwatches.find { it.id == startedId }?.currentMs
            )
            startService(startIntent)
        }



    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if (stopwatches.size != 0) {
            val stopIntent = Intent(this, ForegroundService::class.java)
            stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
            startService(stopIntent)
        }
    }
}