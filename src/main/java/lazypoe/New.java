package lazypoe;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class New {




    private static boolean run = true;
    private static boolean robotWorking = false;
    private static boolean saveDone = false;

    private Robot robot;
    private lazyPoe lazy;
    private Thread thread;

    public New() throws AWTException, IOException {
        robot = new Robot();
        lazy = new lazyPoe();
    }

    public String getClipboardText() throws UnsupportedFlavorException, IOException {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    public void copyToClipboard() throws UnsupportedFlavorException, IOException {

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.delay(5);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }







}
