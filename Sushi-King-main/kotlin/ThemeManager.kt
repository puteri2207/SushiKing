package com.example.sushiking

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View

object ThemeManager {

    private const val PREFS_NAME = "sushiking_theme"
    private const val KEY_COLOR = "theme_color"
    private const val KEY_DARK = "is_dark_mode"

    val lightThemes = listOf(
        ThemeOption("Sushi Red", "#E31E24", "#B91C1C", "#FFF8F0"),
        ThemeOption("Ocean Blue", "#1D4ED8", "#1E3A8A", "#EFF6FF"),
        ThemeOption("Forest Green", "#15803D", "#14532D", "#F0FDF4"),
        ThemeOption("Royal Purple", "#7C3AED", "#4C1D95", "#F5F3FF"),
        ThemeOption("Sunset Orange", "#EA580C", "#9A3412", "#FFF7ED"),
        ThemeOption("Rose Pink", "#BE185D", "#831843", "#FDF2F8"),
        ThemeOption("Teal", "#0F766E", "#134E4A", "#F0FDFA")
    )

    val darkThemes = listOf(
        ThemeOption("Dark Red", "#991B1B", "#7F1D1D", "#1C1917"),
        ThemeOption("Dark Blue", "#1E3A8A", "#172554", "#0F172A"),
        ThemeOption("Dark Green", "#14532D", "#052E16", "#0A1628"),
        ThemeOption("Dark Purple", "#4C1D95", "#2E1065", "#1A0533"),
        ThemeOption("Dark Orange", "#9A3412", "#7C2D12", "#1C0A05"),
        ThemeOption("Dark Rose", "#831843", "#500724", "#1A0512"),
        ThemeOption("Dark Teal", "#134E4A", "#042F2E", "#021B1A")
    )

    data class ThemeOption(
        val name: String,
        val primaryColor: String,
        val primaryDark: String,
        val backgroundColor: String
    )

    fun saveTheme(context: Context, colorIndex: Int, isDark: Boolean) {
        getPrefs(context).edit()
            .putInt(KEY_COLOR, colorIndex)
            .putBoolean(KEY_DARK, isDark)
            .apply()
    }

    fun getColorIndex(context: Context): Int = getPrefs(context).getInt(KEY_COLOR, 0)

    fun isDarkMode(context: Context): Boolean = getPrefs(context).getBoolean(KEY_DARK, false)

    fun getCurrentTheme(context: Context): ThemeOption {
        val index = getColorIndex(context)
        return if (isDarkMode(context)) darkThemes[index] else lightThemes[index]
    }

    fun getPrimaryColor(context: Context): Int {
        return Color.parseColor(getCurrentTheme(context).primaryColor)
    }

    fun getBackgroundColor(context: Context): Int {
        return Color.parseColor(getCurrentTheme(context).backgroundColor)
    }

    // Apply theme to a view (e.g. header background)
    fun applyToView(context: Context, view: View) {
        view.setBackgroundColor(getPrimaryColor(context))
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
