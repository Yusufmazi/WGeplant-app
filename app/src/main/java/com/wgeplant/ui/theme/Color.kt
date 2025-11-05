package com.wgeplant.ui.theme

import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF8E9AD1)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_secondary = Color(0xFFB1BCE9)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_tertiary = Color(0xFF464D6C)
val md_theme_light_error = Color(0xFFFF0000)
val BlueIndicator = Color(0xFF2196F3)
val DarkBlueIndicator = Color(0xFF0D47A1)
val GreenIndicator = Color(0xFF4CAF50)
val DarkGreenIndicator = Color(0xFF1B5E20)
val RedIndicator = Color(0xFFF44336)
val OrangeIndicator = Color(0xFFFF9800)
val PurpleIndicator = Color(0xFF9C27B0)
val RoseIndicator = Color(0xFFF48FB1)
val YellowIndicator = Color(0xFFFFD600)
val GreyIndicator = Color(0xFF9E9E9E)

/**
 * Object holding colors used specifically for event indicators
 * and providing utility functions for color name mapping.
 */
object EventColors {
    /**
     * The default event color used when no specific color is assigned.
     */
    val defaultEventColor = GreyIndicator

    /**
     * A list of all available event indicator colors.
     */
    val allEventColors = listOf(
        BlueIndicator,
        DarkBlueIndicator,
        GreenIndicator,
        DarkGreenIndicator,
        RedIndicator,
        OrangeIndicator,
        PurpleIndicator,
        RoseIndicator,
        YellowIndicator,
        GreyIndicator
    )

    private val colorNames: Map<Color, String> = mapOf(
        BlueIndicator to "Blau",
        DarkBlueIndicator to "Dunkelblau",
        GreenIndicator to "Grün",
        DarkGreenIndicator to "Dunkelgrün",
        RedIndicator to "Rot",
        OrangeIndicator to "Orange",
        PurpleIndicator to "Lila",
        RoseIndicator to "Rosa",
        YellowIndicator to "Gelb",
        GreyIndicator to "Standardfarbe"
    )

    /**
     * Returns the localized name of the given event color.
     *
     * @param color The [Color] to get the name for.
     * @return The German name of the color if found; otherwise an empty string.
     */
    fun getEventColorName(color: Color): String {
        return colorNames[color] ?: ""
    }
}
