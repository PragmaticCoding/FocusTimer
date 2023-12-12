package ca.pragmaticcoding.focustimer

import javafx.beans.property.*
import javafx.scene.Node
import javafx.util.Duration
import java.time.LocalTime


enum class TimerMode {
    REST, FOCUS;
}

enum class TimerStatus {
    STOPPED, RUNNING, PAUSED;
}

enum class TimerSize {
    FULL, MINI;
}

class TimerModel {
    val maxTime: LongProperty = SimpleLongProperty(3600)
    val currentTime: ObjectProperty<LocalTime> = SimpleObjectProperty(LocalTime.now())
    val focusAngle: DoubleProperty = SimpleDoubleProperty(90.0)
    val restAngle: DoubleProperty = SimpleDoubleProperty(90.0)
    val focusTime: ObjectProperty<Duration> = SimpleObjectProperty(Duration.minutes(0.0))
    val restTime: ObjectProperty<Duration> = SimpleObjectProperty(Duration.minutes(0.0))
    val endTime: ObjectProperty<LocalTime> = SimpleObjectProperty(LocalTime.now())
    val activeMode: ObjectProperty<TimerMode> = SimpleObjectProperty(TimerMode.FOCUS)
    val timerStatus: ObjectProperty<TimerStatus> = SimpleObjectProperty(TimerStatus.STOPPED)
    val timerSize: ObjectProperty<TimerSize> = SimpleObjectProperty(TimerSize.FULL)
    val stageX: DoubleProperty = SimpleDoubleProperty(0.0)
    val stageY: DoubleProperty = SimpleDoubleProperty(0.0)
    val playSounds: BooleanProperty = SimpleBooleanProperty(true)
}

fun Duration.toMinSec(): String {
    val wholeMinutes = this.toMinutes().toInt()
    val seconds = this.subtract(Duration.minutes(wholeMinutes.toDouble()))
    return String.format("%02d:%02d", wholeMinutes, seconds.toSeconds().toInt())
}

fun Node.hideWhenSmall(timerModel: TimerModel) {
    visibleProperty().bind(timerModel.timerSize.isEqualTo(TimerSize.FULL))
    managedProperty().bind(timerModel.timerSize.isEqualTo(TimerSize.FULL))
}


