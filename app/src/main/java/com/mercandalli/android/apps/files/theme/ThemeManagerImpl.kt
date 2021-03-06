package com.mercandalli.android.apps.files.theme

import android.content.SharedPreferences

internal class ThemeManagerImpl(
        private val sharedPreferences: SharedPreferences) : ThemeManager {

    private val lightTheme: Theme = LightTheme()
    private val darkTheme: Theme = DarkTheme()
    private var currentThemeIndex = 0
    private var listeners = ArrayList<ThemeManager.OnCurrentThemeChangeListener>()

    init {
        currentThemeIndex = sharedPreferences.getInt("theme", 0)
    }

    override val theme: Theme
        get() = if (currentThemeIndex == 0) lightTheme else darkTheme

    override fun setDarkEnable(enable: Boolean) {
        currentThemeIndex = if (enable) 1 else 0
        sharedPreferences.edit().putInt("theme", currentThemeIndex).apply()
        for (listener in listeners) {
            listener.onCurrentThemeChanged()
        }
    }

    override fun isDarkEnable(): Boolean {
        return currentThemeIndex != 0
    }

    override fun registerThemeListener(listener: ThemeManager.OnCurrentThemeChangeListener) {
        if (listeners.contains(listener)) {
            return
        }
        listeners.add(listener)
    }

    override fun unregisterThemeListener(listener: ThemeManager.OnCurrentThemeChangeListener) {
        listeners.remove(listener)
    }

    companion object {
        @JvmStatic
        val PREFERENCE_NAME = "ThemeManager"
    }
}