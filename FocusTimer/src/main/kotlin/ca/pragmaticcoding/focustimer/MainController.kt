package ca.pragmaticcoding.focustimer

import javafx.animation.Interpolator
import javafx.animation.PauseTransition
import javafx.animation.Transition
import javafx.beans.InvalidationListener
import javafx.scene.layout.Region
import javafx.stage.Stage
import javafx.util.Duration

class MainController {
    private val timerModel = TimerModel()
    private val interactor = MainInteractor(timerModel)
    val view: Region

    init {
        val timerDiskBuilder = TimerDiskBuilder(timerModel)
        view = MainViewBuilder(
            timerDiskBuilder.build(),
            timerModel,
            interactor::handleStartPause,
            interactor::handleStop
        ).build()
        startMainLoop()
        timerModel.timerSize.addListener(InvalidationListener {
            interactor.saveSizeLocation(timerModel.timerSize.value)
            changeSize(timerModel.timerSize.value)
        })
    }

    private fun startMainLoop() {
        PauseTransition(Duration(100.0)).apply {
            setOnFinished {
                interactor.recalculateValues()
                playFromStart()
            }
        }.play()
    }

    fun setInitialLocation(stage: Stage) {
        timerModel.stageX.bind(stage.xProperty())
        timerModel.stageY.bind(stage.yProperty())
    }

    private fun changeSize(newSize: TimerSize) {
        with(view.scene.window as Stage) {
            object : Transition() {
                init {
                    cycleDuration = Duration.millis(600.0)
                    interpolator = Interpolator.LINEAR
                }

                override fun interpolate(frac: Double) {
                    x = interactor.interpolateX(newSize, frac)
                    y = interactor.interpolateY(newSize, frac)
                    height = interactor.interpolateHeight(newSize, frac)
                    width = interactor.interpolateWidth(newSize, frac)
                }
            }.play()
        }
    }
}