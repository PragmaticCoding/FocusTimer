package ca.pragmaticcoding.widgetsfx

import javafx.beans.WeakListener
import javafx.beans.property.StringProperty
import javafx.collections.MapChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ButtonBase
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import java.lang.ref.WeakReference

fun Scene.addWidgetStyles() = apply {
    object {}::class.java.getResource("/css/widgetsfx.css")?.toString()?.let {
        stylesheets += it
        println("found it")
    }
}

fun Scene.addStyleSheet(sheetName: String) = apply {
    object {}::class.java.getResource(sheetName)?.toString()?.let {
        stylesheets += it
        println("Found stylesheet")
    }
}

fun <T : Parent> T.addWidgetStyles() = apply {
    object {}::class.java.getResource("widgetsfx.css")?.toString()?.let { stylesheets += it }
}

enum class TestStyle(val selector: String) {
    BLUE("test-blue"), RED("test-red"), GREEN("test-green")
}

infix fun <T : Node> T.testStyleAs(nodeStyle: TestStyle) = apply { styleClass += nodeStyle.selector }

infix fun <T : Region> T.padWith(padSize: Double): T = apply { padding = Insets(padSize) }

infix fun <T : Node> T.addStyle(newStyleClass: String): T = apply { styleClass += newStyleClass }

fun textFieldOf(value: StringProperty) = TextField().apply { textProperty().bind(value) }

infix fun TextField.bindTo(value: StringProperty) = apply { textProperty().bind(value) }

fun buttonOf(text: String, handler: EventHandler<ActionEvent>) = Button(text) addAction handler

operator fun Pane.plusAssign(newChild: Node) {
    children += newChild
}

infix fun HBox.alignTo(pos: Pos): HBox = apply { alignment = pos }

infix fun <T : ButtonBase> T.addAction(eventHandler: EventHandler<ActionEvent>): T = apply { onAction = eventHandler }

class MapConversionListener<SourceTypeKey, SourceTypeValue, TargetType>(
    targetList: MutableList<TargetType>,
    val converter: (SourceTypeKey, SourceTypeValue) -> TargetType
) :
    MapChangeListener<SourceTypeKey, SourceTypeValue>, WeakListener {
    internal val targetRef: WeakReference<MutableList<TargetType>> = WeakReference(targetList)
    internal val sourceToTarget = HashMap<SourceTypeKey, TargetType>()

    override fun onChanged(change: MapChangeListener.Change<out SourceTypeKey, out SourceTypeValue>) {
        val list = targetRef.get()
        if (list == null) {
            change.map.removeListener(this)
            sourceToTarget.clear()
        } else {
            if (change.wasRemoved()) {
                list.remove(sourceToTarget[change.key])
                sourceToTarget.remove(change.key)
            }
            if (change.wasAdded()) {
                val converted = converter(change.key, change.valueAdded)
                sourceToTarget[change.key] = converted
                list.add(converted)
            }
        }
    }

    override fun wasGarbageCollected() = targetRef.get() == null

    override fun hashCode() = targetRef.get().hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        val ourList = targetRef.get() ?: return false

        if (other is MapConversionListener<*, *, *>) {
            val otherList = other.targetRef.get()
            return ourList === otherList
        }
        return false
    }
}