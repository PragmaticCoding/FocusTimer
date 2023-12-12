package ca.pragmaticcoding.focustimer

import ca.pragmaticcoding.widgetsfx.addStyle
import javafx.beans.binding.Bindings
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Line
import javafx.scene.text.Text
import javafx.util.Builder
import kotlin.math.*


class TimerDiskBuilder(private val model: TimerModel) : Builder<Region> {

    private val radiusProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    private val centreXProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    private val centreYProperty: DoubleProperty = SimpleDoubleProperty(0.0)

    override fun build(): Region = Pane().apply {
        radiusProperty.bind(
            Bindings.createDoubleBinding(
                { min(width, height) / 2.8 },
                widthProperty(),
                heightProperty()
            )
        )
        centreXProperty.bind(widthProperty().divide(2.0))
        centreYProperty.bind(heightProperty().divide(2.0))

        children += createArc(model.focusAngle).apply { visibleProperty().bind(model.activeMode.isEqualTo(TimerMode.FOCUS)) } addStyle "focus-disk"
        children += createArc(model.restAngle).apply { visibleProperty().bind(model.activeMode.isEqualTo(TimerMode.REST)) } addStyle "rest-disk"
        setOnMousePressed { evt -> mouseHandler(evt) }
        setOnMouseDragged { evt -> mouseHandler(evt) }
        for (x in 0..11) {
            children += createLine(Math.PI + ((Math.PI / 6) * x), 9.0, 3.0, false) addStyle "big-tick"
        }
        for (x in 0..59) {
            children += createLine(Math.PI + ((Math.PI / 30) * x), 5.0, 2.0, true) addStyle "small-tick"
        }
        for (x in 1..12) {
            children += createNumber(Math.PI + ((Math.PI / 6) * x), x * 5)
        }
        this.isPickOnBounds = true
    }

    private fun createArc(modelAngle: DoubleProperty) = Arc().apply {
        centerXProperty().bind(centreXProperty)
        centerYProperty().bind(centreYProperty)
        radiusXProperty().bind(radiusProperty)
        radiusYProperty().bind(radiusProperty)
        startAngle = 90.0
        lengthProperty().bind(modelAngle)
        type = ArcType.ROUND
        effect = DropShadow()
    }

    private fun createNumber(angle: Double, x: Int) = HBox().apply {
        children += Text((x).toString()) addStyle "disk-number"
        val gap = 25.0
        translateXProperty().bind(
            centreXProperty.add((radiusProperty.add(gap)).multiply(sin(angle)))
                .subtract(widthProperty().divide(2))
        )
        translateYProperty().bind(
            centreYProperty.add((radiusProperty.add(gap)).multiply(cos(angle)))
                .subtract(heightProperty().divide(2))
        )
        minWidth = 40.0
        alignment = Pos.CENTER
        visibleProperty().bind(model.timerSize.isEqualTo(TimerSize.FULL))
        managedProperty().bind(model.timerSize.isEqualTo(TimerSize.FULL))
    }

    private fun createLine(angle: Double, length: Double, widthValue: Double, hideWhenSmall: Boolean) = Line().apply {
        val gap = 4.0
        startXProperty().bind(centreXProperty.add((radiusProperty.add(gap)).multiply(sin(angle))))
        startYProperty().bind(centreYProperty.add((radiusProperty.add(gap)).multiply(cos(angle))))
        endXProperty().bind(centreXProperty.add((radiusProperty.add(length + gap)).multiply(sin(angle))))
        endYProperty().bind(centreYProperty.add((radiusProperty.add(length + gap)).multiply(cos(angle))))
        strokeWidth = widthValue
        if (hideWhenSmall) {
            hideWhenSmall(model)
        }
    }

    private fun mouseHandler(evt: MouseEvent) {
        if (model.timerStatus.value == TimerStatus.STOPPED && clickedInDisk(evt.x, evt.y)) {
            calculateAngleFromMouse(evt.x, evt.y)
            when (model.activeMode.value) {
                TimerMode.REST -> model.restAngle.value = calculateAngleFromMouse(evt.x, evt.y)
                TimerMode.FOCUS -> model.focusAngle.value = calculateAngleFromMouse(evt.x, evt.y)
            }
            evt.consume()
        }
    }

    private fun clickedInDisk(mouseX: Double, mouseY: Double): Boolean {
        val offsetRadius = sqrt((mouseX - centreXProperty.value).pow(2.0) + (mouseY - centreYProperty.value).pow(2.0))
        return (offsetRadius <= radiusProperty.value)
    }

    private fun calculateAngleFromMouse(mouseX: Double, mouseY: Double): Double {
        var degree = atan2(-(mouseX - centreXProperty.value), -(mouseY - centreYProperty.value)) / Math.PI * 180
        if (degree < 0) degree += 360.0
        val snapDegree = 360.0 / 60.0
        return ((degree + snapDegree / 2) / snapDegree).toInt() * snapDegree
    }
}