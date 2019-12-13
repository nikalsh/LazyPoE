package lazypoe;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinDef;

import static lazypoe.WindowFocus.User32DLL.GetForegroundWindow;
import static lazypoe.WindowFocus.User32DLL.GetWindowTextW;

public class WindowFocus {

    private static final int MAX_TITLE_LENGTH = 1024;

    static class User32DLL {
        static {
            Native.register("user32");
        }

        public static native WinDef.HWND GetForegroundWindow();

        public static native int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
    }

    public static boolean PoEIsOpen() {
        String name = "";
        if (Platform.isWindows()) {
            char[] buffer = new char[MAX_TITLE_LENGTH * 2];
            GetWindowTextW(GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
            name = Native.toString(buffer);
        }
        return name.equals("Path of Exile");
    }

}
