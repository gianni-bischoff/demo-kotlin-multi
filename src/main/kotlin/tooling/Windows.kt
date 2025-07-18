package tooling

import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND

fun ComposeWindow.makeWindowClickThrough() {
    this.background = java.awt.Color(0, 0, 0, 0)

    // On Windows, this makes the window click-through
    com.sun.jna.Platform.isWindows().let {
        try {
            val WIN32_GWL_EXSTYLE = -20
            val WS_EX_LAYERED = 0x00080000
            val WS_EX_TRANSPARENT = 0x00000020

            val hwnd = HWND(Native.getWindowPointer(this))
            val windowsUser32 = com.sun.jna.platform.win32.User32.INSTANCE

            val currentStyle = windowsUser32.GetWindowLong(hwnd, WIN32_GWL_EXSTYLE)
            windowsUser32.SetWindowLong(hwnd, WIN32_GWL_EXSTYLE,
                currentStyle.or(WS_EX_LAYERED).or(WS_EX_TRANSPARENT))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}