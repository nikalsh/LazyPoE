package lazypoe;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinDef;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static lazypoe.Utils.User32DLL.GetForegroundWindow;
import static lazypoe.Utils.User32DLL.GetWindowTextW;

public class Utils {
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

    public static GraphicsDevice[] getAllGraphicsDevices() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    }

    public static void BItoFile(BufferedImage bi, String fileName) {
        try {
            ImageIO.write(bi, "png", new File(fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Mat bufferedImageToMat(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public static BufferedImage matToBufferedImage(Mat matrix) throws Exception {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[] = mob.toArray();

        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }
}
