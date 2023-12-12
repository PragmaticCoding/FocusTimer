package ca.pragmaticcoding.focustimer

import javafx.beans.InvalidationListener
import javafx.scene.media.AudioClip
import javafx.util.Duration
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class MainInteractor(private val model: TimerModel) {

    private var miniX = 0.0
    private var miniY = 0.0
    private var fullX = 0.0
    private var fullY = 0.0
    private var focusEndTime: LocalTime = LocalTime.now()
    private var restEndTime: LocalTime = LocalTime.now()
    private var firstPulse = true
    private var focusDurationAtStart: Duration = Duration.millis(0.0)
    private var restDurationAtStart: Duration = Duration.millis(0.0)

    init {
        model.timerStatus.addListener(InvalidationListener {
            if (model.timerStatus.value == TimerStatus.RUNNING) {
                focusEndTime = calcFocusEndTime()
                restEndTime = calcRestEndTime()
            }
        })
    }

    fun recalculateValues() {
        model.currentTime.value = LocalTime.now()
        if (firstPulse) {
            miniX = model.stageX.value
            fullX = model.stageX.value
            miniY = model.stageY.value
            fullY = model.stageY.value
            firstPulse = false;
        }
        if (model.timerStatus.value != TimerStatus.RUNNING) {
            model.focusTime.value = calculateDuration(model.focusAngle.value)
            model.restTime.value = calculateDuration(model.restAngle.value)
            if (model.activeMode.value == TimerMode.FOCUS) {
                model.endTime.value = calcFocusEndTime()
            } else {
                model.endTime.value = calcRestEndTime()
            }
        } else {
            if (model.activeMode.value == TimerMode.FOCUS) {
                model.endTime.value = focusEndTime
                model.focusTime.value = calculateDuration(model.currentTime.value, focusEndTime)
                model.focusAngle.value = calculateAngle(model.focusTime.value)
                if (model.currentTime.value.isAfter(focusEndTime)) {
                    model.activeMode.value = TimerMode.REST
                    if (model.playSounds.value) {
                        val mediaFile = FocusTimer::class.java.getResource("audios/notification.mp3").toString()
                        AudioClip(mediaFile).play()
                    }
                }
            } else {
                model.endTime.value = restEndTime
                model.restTime.value = calculateDuration(model.currentTime.value, restEndTime)
                model.restAngle.value = calculateAngle(model.restTime.value)
                if (model.currentTime.value.isAfter(restEndTime)) {
                    model.timerStatus.value = TimerStatus.STOPPED
                }
            }
        }
    }

    private fun calcRestEndTime(): LocalTime = calcFocusEndTime()
        .plusSeconds(model.restTime.value.toSeconds().toLong())

    private fun calcFocusEndTime(): LocalTime =
        model.currentTime.value.plusSeconds(model.focusTime.value.toSeconds().toLong())

    private fun calculateDuration(angle: Double) = Duration.seconds(model.maxTime.value * angle / 360)
    private fun calculateAngle(duration: Duration) = duration.toSeconds() * 360 / model.maxTime.value
    private fun calculateDuration(startTime: LocalTime, endTime: LocalTime) = Duration.seconds(
        startTime.until(
            endTime,
            ChronoUnit.SECONDS
        ).toDouble()
    )

    fun saveSizeLocation(newSize: TimerSize) {
        if (newSize == TimerSize.FULL) {
            miniX = model.stageX.value
            miniY = model.stageY.value
        } else {
            fullX = model.stageX.value
            fullY = model.stageY.value
        }
        println("New Locations: $miniX,$miniY  $fullX,$fullY")
    }

    fun interpolateX(newSize: TimerSize, frac: Double): Double {
        return if (newSize == TimerSize.MINI) {
            fullX + (frac * (miniX - fullX))
        } else {
            miniX + (frac * (fullX - miniX))
        }
    }

    fun interpolateY(newSize: TimerSize, frac: Double): Double {
        return if (newSize == TimerSize.MINI) {
            fullY + (frac * (miniY - fullY))
        } else {
            miniY + (frac * (fullY - miniY))
        }
    }

    fun handleStartPause() {
        when (model.timerStatus.value ?: TimerStatus.STOPPED) {
            TimerStatus.STOPPED -> {
                focusDurationAtStart = model.focusTime.value
                restDurationAtStart = model.restTime.value
                model.activeMode.value = TimerMode.FOCUS
                model.timerStatus.value = TimerStatus.RUNNING
                model.timerSize.value = TimerSize.MINI
            }

            TimerStatus.RUNNING -> {
                model.timerStatus.value = TimerStatus.PAUSED
            }

            TimerStatus.PAUSED -> {
                model.timerStatus.value = TimerStatus.RUNNING
            }
        }
    }

    fun handleStop() {
        if (model.timerStatus.value != TimerStatus.STOPPED) {
            model.timerStatus.value = TimerStatus.STOPPED
            model.focusTime.value = focusDurationAtStart
            model.focusAngle.value = calculateAngle(focusDurationAtStart)
            model.restTime.value = restDurationAtStart
            model.restAngle.value = calculateAngle(restDurationAtStart)
        }
    }

    fun interpolateHeight(newSize: TimerSize, frac: Double): Double = if (newSize == TimerSize.MINI) {
        120.0 + ((1 - frac) * 240.0)
    } else {
        120.0 + ((frac) * 340.0)
    }

    fun interpolateWidth(newSize: TimerSize, frac: Double): Double = if (newSize == TimerSize.MINI) {
        120.0 + ((1 - frac) * 240.0)
    } else {
        120.0 + ((frac) * 180.0)
    }
}