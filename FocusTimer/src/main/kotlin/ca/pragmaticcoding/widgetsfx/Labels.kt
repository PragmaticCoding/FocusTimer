package ca.pragmaticcoding.widgetsfx

import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableStringValue
import javafx.scene.control.Label
import javafx.scene.control.Labeled

enum class LabelStyle(val selector: String) {
    PROMPT("label-prompt"), HEADING("label-heading")
}

infix fun <T : Labeled> T.styleAs(labelStyle: LabelStyle) = apply { styleClass += labelStyle.selector }
infix fun <T : Labeled> T.styleAs(labelStyle: String) = apply { styleClass += labelStyle }

infix fun <T : Labeled> T.bindTo(value: ObservableStringValue) = apply { textProperty().bind(value) }

fun promptOf(value: ObservableStringValue) = Label() styleAs LabelStyle.PROMPT bindTo value
fun promptOf(value: String) = Label(value) styleAs LabelStyle.PROMPT

fun headingOf(value: String) = Label() styleAs LabelStyle.HEADING

operator fun Labeled.plusAssign(otherProperty: StringProperty) = run { textProperty() += otherProperty }

