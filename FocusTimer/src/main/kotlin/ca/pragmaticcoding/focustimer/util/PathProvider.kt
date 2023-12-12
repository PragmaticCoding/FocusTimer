package ca.pragmaticcoding.focustimer.util

import java.nio.file.FileSystems
import java.util.*

object PathProvider {
    val appDataPath: String
        get() {
            val osName = System.getProperty("os.name").uppercase(Locale.getDefault())
            var appDataPath: String? = null
            var folderPath: String? = null
            if (osName.contains("WINDOWS")) {
                appDataPath = System.getenv("APPDATA")
                folderPath = "FocusTimer"
            } else if (osName.contains("LINUX") || osName.contains("MAC")) {
                appDataPath = System.getProperty("user.home")
                folderPath = ".FocusTimer"
            } else {
                return "unsupported OS"
            }
            return appDataPath + FileSystems.getDefault().separator + folderPath
        }
}