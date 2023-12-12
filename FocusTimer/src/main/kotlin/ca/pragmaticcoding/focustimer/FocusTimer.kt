package ca.pragmaticcoding.focustimer

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.stage.StageStyle

class FocusTimer : Application() {
    override fun start(stage: Stage) {
        val controller = MainController()
        val scene = Scene(controller.view, 300.0, 460.0).apply {
            FocusTimer::class.java.getResource("css/focus_timer.css")?.toString()?.let {
                stylesheets += it
                println("Found stylesheet")
            }
            fill = Color.TRANSPARENT

        }
        stage.title = "Focus Timer"
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.setAlwaysOnTop(true)
        stage.scene = scene
        stage.onShown = EventHandler { controller.setInitialLocation(stage) }
        stage.show()
    }

    override fun init() {
        System.setProperty("prism.lcdText", "false")
        Font.loadFont(javaClass.getResourceAsStream("fonts/Inter-Regular.ttf"), 11.0)
        super.init()
    }
}

fun main() {
    Application.launch(FocusTimer::class.java)
}