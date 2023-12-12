package ca.pragmaticcoding.focustimer

import ca.pragmaticcoding.widgetsfx.addStyle
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.util.Builder
import javafx.util.converter.LocalTimeStringConverter
import java.time.format.FormatStyle

class MainViewBuilder(
    private val timerDisk: Region,
    private val model: TimerModel,
    private val pauseStartHandler: Runnable,
    private val stopHandler: Runnable
) : Builder<Region> {
    override fun build(): Region = BorderPane().apply {
        top = createTop()
        center = timerDisk
        bottom = createBottom()
        configureMouseHandlers(this)
    } addStyle "main-screen"

    private fun configureMouseHandlers(region: Region) = with(region) {
        var xOffset = 0.0
        var yOffset = 0.0
        var didDrag = false
        onMousePressed = EventHandler { evt ->
            xOffset = evt.sceneX
            yOffset = evt.sceneY
            didDrag = false;
        }
        onMouseClicked = EventHandler {
            if (!didDrag) {
                val timerSize = model.timerSize.value
                if ((timerSize == TimerSize.MINI) ||
                    (model.timerStatus.value != TimerStatus.STOPPED)
                ) {
                    model.timerSize.value =
                        if (timerSize == TimerSize.FULL) TimerSize.MINI else TimerSize.FULL
                }
            }
        }
        onMouseDragged = EventHandler { evt ->
            val stage = scene.window as Stage
            stage.x = evt.screenX - xOffset;
            stage.y = evt.screenY - yOffset;
            didDrag = true
        }
        onScroll = EventHandler { evt ->
            println("Scroll: ${evt.deltaY}")
            opacity += (evt.deltaY / 300.0)
            if (opacity <= 0.0) opacity = 0.10
            if (opacity > 1.0) opacity = 1.0
        }
    }

    private fun createBottom(): Node = VBox(8.0, createTimeBox(), createButtonBox())

    private fun createTop(): Node = AnchorPane().apply {
        val appImage = getIcon("app_icon.png", 24.0)
        AnchorPane.setLeftAnchor(appImage, 2.0)
        children += appImage
        styleClass += "header-bar"
        val buttonBox = HBox(4.0,
            Button("", getIcon("minimize.png", 24.0)).apply {
                onAction = EventHandler { (this.scene.window as Stage).isIconified = true }
                styleClass += "action-button"
            }, Button("", getIcon("full.png", 24.0)).apply {
                onAction = EventHandler { (scene.window as Stage).isFullScreen = !(scene.window as Stage).isFullScreen }
                styleClass += "action-button"
            },
            Button("", getIcon("close.png", 24.0)).apply {
                onAction = EventHandler { Platform.exit() }
                styleClass += "action-button"
            }
        )
        AnchorPane.setRightAnchor(buttonBox, 2.0)
        children += buttonBox
        hideWhenSmall(model)
    }

    private fun createTimeBox() = HBox(20.0, focusTimeLabel(), restTimeLabel(), completionTimeLabel()).apply {
        alignment = Pos.CENTER
        hideWhenSmall(model)
    }

    private fun focusTimeLabel() = Label().apply {
        onMouseClicked = EventHandler {
            if (model.timerStatus.value == TimerStatus.STOPPED) model.activeMode.value = TimerMode.FOCUS
        }
        textProperty().bind(
            Bindings.createStringBinding(
                { model.focusTime.value.toMinSec() },
                model.focusTime
            )
        )
        styleClass += "focus-time"
    }

    private fun restTimeLabel() = Label().apply {
        onMouseClicked = EventHandler {
            if (model.timerStatus.value == TimerStatus.STOPPED) model.activeMode.value = TimerMode.REST
        }
        textProperty().bind(
            Bindings.createStringBinding(
                { model.restTime.value.toMinSec() },
                model.restTime
            )
        )
        styleClass += "rest-time"
    }

    private fun completionTimeLabel() = Label().apply {
        val converter = LocalTimeStringConverter(FormatStyle.SHORT)
        textProperty().bind(
            Bindings.createStringBinding(
                { converter.toString(model.endTime.value) },
                model.endTime
            )
        )
        styleClass += "completion-time"
    }

    private fun createButtonBox() = HBox(10.0, stopButton(), playPauseButton(), soundButton()).apply {
        alignment = Pos.CENTER
        hideWhenSmall(model)
    }

    private fun stopButton() = actionButton(stopHandler).apply {
        graphic = getIcon("stop.png", 48.0)
    }

    private fun playPauseButton() = actionButton(pauseStartHandler).apply {
        val playIcon = getIcon("play.png", 48.0)
        val pauseIcon = getIcon("pause.png", 48.0)
        graphicProperty().bind(
            Bindings.createObjectBinding(
                { if (model.timerStatus.value == TimerStatus.RUNNING) pauseIcon else playIcon },
                model.timerStatus
            )
        )
    }

    private fun actionButton(handler: Runnable) = Button().apply {
        onAction = EventHandler { handler.run() }
        styleClass += "action-button"
    }

    private fun soundButton() = ToggleButton().apply {
        val onImageView = getIcon("volume_on.png", 48.0)
        val offImageView = getIcon("volume_off.png", 48.0)
        graphicProperty().bind(Bindings.createObjectBinding({
            if (isSelected) onImageView else offImageView
        }, selectedProperty()))
        selectedProperty().bindBidirectional(model.playSounds)
        styleClass += "action-button"
    }


    private fun getIcon(fileName: String, size: Double) =
        ImageView(FocusTimer::class.java.getResource("icons/$fileName")?.toExternalForm()).apply {
            fitHeight = size
            fitWidth = size
        }
}