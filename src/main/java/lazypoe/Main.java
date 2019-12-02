package lazypoe;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class Main {

    private static boolean run = true;
    private static boolean robotWorking = false;
    private static boolean saveDone = false;
    private static String CURRENCY_TAB = "$";
    private static String MAP_TAB = "m";
    private static String FRAGMENT_TAB = "f";
    private static String DIV_TAB = "d";

    private Robot robot;
    private lazyPoe lazy;
    private Thread thread;
    private GlobalKeyboardHook globalKeyboardHook;


    public Main() throws AWTException, UnsupportedFlavorException, IOException {
        robot = new Robot();
        lazy = new lazyPoe();
        globalKeyboardHook = new GlobalKeyboardHook(true);
//        Application.launch(GUI.class);

        robot.setAutoDelay(7);

        new Thread(() -> {

            while (true) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.format("x: %s y: %s%n", p.getX(), p.getY());
            }


        }).start();

        globalKeyboardHook.addKeyListener(new GlobalKeyAdapter() {

//            @Override
//            public void keyPressed(GlobalKeyEvent event) {
//
//                if (event.isControlPressed() && event.isShiftPressed()) {
//                    if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_F) {
//                        try {
//                            rt.saveCurrentItem();
//                        } catch (UnsupportedFlavorException ex) {
//                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (IOException ex) {
//                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//
//                    if (event.isControlPressed() && event.isShiftPressed()) {
//                        if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_R) {
//                            try {
//                                rt.removeCurrentItem();
//                            } catch (UnsupportedFlavorException ex) {
//                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                            } catch (IOException ex) {
//                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
//                    }
//                }
//            }


            public boolean CTRL_X(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_X && event.isControlPressed();
            }

            public boolean CTRL_D(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_D && event.isControlPressed() && !robotWorking;
            }


            public boolean CTRL_SHIFT_X(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_X && event.isControlPressed() && event.isShiftPressed();
            }

            public boolean CTRL_R(GlobalKeyEvent event) {
                return event.getVirtualKeyCode()
                        == GlobalKeyEvent.VK_R && event.isControlPressed();
            }

            public boolean CTRL_C(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_C && event.isControlPressed();
            }

            public boolean UP(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_UP;
            }

            public boolean DOWN(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_DOWN;
            }

            public boolean LEFT(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_LEFT;
            }

            public boolean RIGHT(GlobalKeyEvent event) {
                return event.getVirtualKeyCode() == GlobalKeyEvent.VK_RIGHT;
            }

            @Override
            public void keyReleased(GlobalKeyEvent event) {

                if (CTRL_C(event)) {

                    try {
                        System.out.println(getClipboardText());
                    } catch (UnsupportedFlavorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (CTRL_X(event)) {
                    robotWorking = false;
                    System.out.println("action cancelled");
                }

                if (CTRL_D(event)) {
                    robotWorking = true;
                    new Thread(() -> {
                        while (robotWorking) {

//                            dumpAllInventoryItems();
                            try {
                                analyzeInventory();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (UnsupportedFlavorException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                if (UP(event)) {
                    double y = MouseInfo.getPointerInfo().getLocation().getY();
                    double x = MouseInfo.getPointerInfo().getLocation().getX();
                    robot.mouseMove((int) x, (int) y - 1);
                }
                if (DOWN(event)) {
                    double y = MouseInfo.getPointerInfo().getLocation().getY();
                    double x = MouseInfo.getPointerInfo().getLocation().getX();
                    robot.mouseMove((int) x, (int) y + 1);

                }
                if (LEFT(event)) {
                    double y = MouseInfo.getPointerInfo().getLocation().getY();
                    double x = MouseInfo.getPointerInfo().getLocation().getX();

                    robot.mouseMove((int) x - 1, (int) y);

                }
                if (RIGHT(event)) {
                    double y = MouseInfo.getPointerInfo().getLocation().getY();
                    double x = MouseInfo.getPointerInfo().getLocation().getX();
                    robot.mouseMove((int) x + 1, (int) y);
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
            globalKeyboardHook.shutdownHook();
        }
    }


    private void analyzeInventory() throws IOException, UnsupportedFlavorException {

        int x = 1297;
        int y = 615;
        System.out.println("analyzing..");
        OUTER:
        for (int row = 0; row < 5; row++) {

            for (int col = 0; col < 12; col++) {
                if (!robotWorking) {
                    System.out.println("action cancelled.");
                    break OUTER;
                }
                robot.mouseMove(x, y);
                copyToClipboard();
                getClipboardText();
                //if clipboard is empty
                //remember the empty slot
                //ignore it in the next tab analyze session


                x += 52;
            }
            x = 1297;
            y += 52;
        }
        System.out.println("done.");
        robotWorking = false;
    }



    private void dumpInCurrencyTab() {
        int y = 144;
        int x = 37 + 29;

        for (int row = 0; row < 5; row++) {

            for (int col = 0; col < 12; col++) {

                robot.mouseMove(x, y);
                robot.mousePress(InputEvent.BUTTON2_MASK);
                robot.mouseRelease(InputEvent.BUTTON2_MASK);
            }
        }

    }

    public void dumpAllInventoryItems() {

        int x = 1297;
        int y = 615;
        System.out.println("dumping..");
        OUTER:
        for (int row = 0; row < 5; row++) {

            for (int col = 0; col < 12; col++) {
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
                System.out.println(getClipboardText());

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

    public String getClipboardText() throws UnsupportedFlavorException, IOException {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    public void copyToClipboard() {

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.delay(5);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void main(String[] args) throws AWTException, UnsupportedFlavorException, IOException {
        Main main = new Main();
    }


}
