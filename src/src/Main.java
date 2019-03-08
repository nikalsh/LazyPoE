package src;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class Main {

    private static boolean run = true;
    private static boolean robotWorking = false;
    private static boolean saveDone = false;

    Robot robot;
    lazyPoe lazy;
    Thread thread;

    public Main() throws AWTException, IOException {
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

    public void dumpAllInventoryItems() {

        int x = 1297;
        int y = 615;
        System.out.println("dumping..");
        OUTER:
        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 12; j++) {
                if (!robotWorking) {
                    System.out.println("action cancelled.");
                    break OUTER;
                }
                robot.mouseMove(x, y);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                x += 52;
            }
            x = 1297;
            y += 52;
        }
        System.out.println("done.");
        robotWorking = false;
    }

    public void evalAndSellInventoryItems() throws UnsupportedFlavorException, IOException {

        int x = 1297;
        int y = 615;
        OUTER:
        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 12; j++) {
                if (!robotWorking) {
                    break OUTER;
                }
                robot.mouseMove(x, y);
                copyToClipboard();

                if (lazy.evalItem(getClipboardText())) {
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                }
                x += 52;
            }
            x = 1297;
            y += 52;
        }
        robotWorking = false;
    }

    public void eval() throws UnsupportedFlavorException, IOException {
        copyToClipboard();
        System.out.println(lazy.evalItem(getClipboardText()));
    }

    public void saveCurrentItem() throws UnsupportedFlavorException, IOException {
        copyToClipboard();
        lazy.tempSave(getClipboardText());
    }

    public void iterateInventory() {
        int x = 1297;
        int y = 615;

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 12; j++) {
                robot.mouseMove(x, y);
                x += 52;
            }
            x = 1297;
            y += 52;
        }
    }

    public void removeCurrentItem() throws UnsupportedFlavorException, IOException {
        copyToClipboard();
        lazy.removeFromMemory(getClipboardText());
    }

    public static void main(String[] args) throws AWTException, UnsupportedFlavorException, IOException {

        Main rt = new Main();
        rt.robot.setAutoDelay(12);

        GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true);
        keyboardHook.addKeyListener(new GlobalKeyAdapter() {

            @Override
            public void keyPressed(GlobalKeyEvent event) {

                if (event.isControlPressed() && event.isShiftPressed()) {
                    if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_F) {
                        try {
                            rt.saveCurrentItem();
                        } catch (UnsupportedFlavorException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    if (event.isControlPressed() && event.isShiftPressed()) {
                        if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_R) {
                            try {
                                rt.removeCurrentItem();
                            } catch (UnsupportedFlavorException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }

            @Override
            public void keyReleased(GlobalKeyEvent event) {

                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_X && event.isControlPressed()) {
                    robotWorking = false;
                    System.out.println("action cancelled");
                }

                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_D && event.isControlPressed() && !robotWorking) {
                    robotWorking = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (robotWorking) {

                                rt.dumpAllInventoryItems();

                            }
                        }
                    }).start();
                }

                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_S && event.isControlPressed() && !robotWorking) {
                    robotWorking = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (robotWorking) {

                                try {
                                    rt.evalAndSellInventoryItems();
                                } catch (UnsupportedFlavorException ex) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
//                                    Logger.getcLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }
                    }).start();
                }

                if (event.getVirtualKeyCode()
                        == GlobalKeyEvent.VK_X && event.isControlPressed() && event.isShiftPressed()) {

                    System.out.print("saving..");
                    rt.lazy.forceSave();
                    System.out.println(" done.");
                    run = false;
                }

                if (event.getVirtualKeyCode()
                        == GlobalKeyEvent.VK_R && event.isControlPressed()) {

                    try {
                        rt.eval();
                    } catch (UnsupportedFlavorException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        });

        try {
            while (run) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            //Do nothing
        } finally {
            System.out.println("shutting down..");
            keyboardHook.shutdownHook();
        }
    }

}
