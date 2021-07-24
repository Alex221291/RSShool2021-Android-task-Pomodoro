package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.StopwatchItemBinding

import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combineTransform

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root), LifecycleObserver {

    private var timer: CountDownTimer? = null
    private var startTime = 0L

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.customView.setPeriod(stopwatch.startMs)
        if(stopwatch.isStarted) {
            setIsRecyclable(false)
        } else if(!isRecyclable) {
            setIsRecyclable(true)
        }
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            if(!isRecyclable) setIsRecyclable(true)
            binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.white))
            binding.startPauseButton.isEnabled = true
            listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding.startPauseButton.text = resources.getText(R.string.stop)
        binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.white))

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getText(R.string.start)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.startMs, UNIT_THOUSAND_MS) {
            val interval = UNIT_THOUSAND_MS
            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval + System.currentTimeMillis() - System.currentTimeMillis()
                binding.customView.setCurrent(stopwatch.startMs - stopwatch.currentMs)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }

            override fun onFinish() {
                binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.fire_brick))
                binding.startPauseButton.isEnabled = false
                binding.blinkingIndicator.isVisible = false
                binding.startPauseButton.text = "X"
                binding.stopwatchTimer.text = stopwatch.startMs.displayTime()
                listener.stop(stopwatch.id, stopwatch.currentMs)
                setIsRecyclable(true)
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return FINISH_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val FINISH_TIME = "00:00:00"
        private const val UNIT_THOUSAND_MS = 1000L
        private const val INTERVAL = 1000L
    }
}