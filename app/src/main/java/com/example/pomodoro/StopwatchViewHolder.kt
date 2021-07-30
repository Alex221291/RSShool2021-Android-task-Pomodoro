package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.StopwatchItemBinding

import androidx.lifecycle.*


class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root), LifecycleObserver {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {

        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

        binding.customView.setPeriod(stopwatch.startMs)
        binding.customView.setCurrent(stopwatch.startMs - stopwatch.currentMs)

        if(stopwatch.currentMs == -1L) {
            binding.constraintLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context,
                R.color.fire_brick))
            binding.startPauseButton.isEnabled = false
        } else {
            binding.constraintLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context,
                R.color.white))
            binding.startPauseButton.isEnabled = true
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
            if (stopwatch.isStarted) stopTimer(stopwatch)
            binding.constraintLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context,
            R.color.white))
            binding.startPauseButton.isEnabled = true
            listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getText(R.string.stop)

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
        return object : CountDownTimer(stopwatch.currentMs, INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                binding.stopwatchTimer.text = millisUntilFinished.displayTime()
                stopwatch.currentMs = millisUntilFinished
                binding.customView.setCurrent(stopwatch.startMs - stopwatch.currentMs + INTERVAL + System.currentTimeMillis() - System.currentTimeMillis())
            }

            override fun onFinish() {
                binding.constraintLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context,
                    R.color.fire_brick))
                stopwatch.currentMs = -1L
                listener.stop(stopwatch.id, stopwatch.currentMs)
                binding.startPauseButton.text = resources.getText(R.string.finish)
                binding.startPauseButton.isEnabled = false
                binding.stopwatchTimer.text = stopwatch.startMs.displayTime()
                binding.customView.setCurrent(0L)
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
        private const val INTERVAL = 1000L
    }
}