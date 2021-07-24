package com.example.pomodoro

import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import android.widget.Toast
import androidx.annotation.RequiresApi

import android.content.Intent
import androidx.lifecycle.*
import kotlinx.coroutines.*


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

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            try {
                time = binding.minutesNewTimer.text.toString().toLong()
                if(time != 0L) {
                    stopwatches.add(
                        Stopwatch(
                            nextId++,
                            time * 1000L * 60L,
                            time * 1000L * 60L,
                            false
                        )
                    )
                    stopwatchAdapter.submitList(stopwatches.toList())
                }
                else Toast.makeText(baseContext, "Invalid data", Toast.LENGTH_LONG).show()
            }
            catch (e:NumberFormatException)
            {
                Toast.makeText(baseContext, "Invalid data", Toast.LENGTH_LONG).show()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun start(id: Int) {
        startedId= id
        changeStopwatch(id, null , true)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun stop(id: Int, currentMs: Long) {
        if (id == startedId) startedId = -1
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        if (id == startedId) startedId = -1
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        stopwatches.replaceAll {
            when {
                it.id == id -> Stopwatch(it.id, it.startMs, currentMs ?: it.currentMs, isStarted)
                it.isStarted -> Stopwatch(it.id, it.startMs, currentMs ?: it.currentMs, false)
                else -> {it}
            }
        }
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private var back_pressed: Long = 0

    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(baseContext, "Press once again to exit!", Toast.LENGTH_SHORT).show()
        }
        back_pressed = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, time)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}