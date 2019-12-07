package lazypoe;

import com.google.common.collect.ImmutableMap;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final Dimension RESOLUTION = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Rectangle FULLSCREEN = new Rectangle(RESOLUTION);

    private static boolean run = true;
    private static boolean robotIsWorking = false;

    private static String CURRENCY_TAB = "c";
    private static String DIVINATION_CARD_TAB = "d";
    private static String MAP_TAB = "m";
    private static String ESSENCE_TAB = "e";
    private static String FRAGMENT_TAB = "f";
    private static final String CURRENCY = "Currency";
    private static final String DIVINATION_CARD = "Divination Card";
    private static final String MAP = "Map";
    private static final String ESSENCE = "Essence";
    private static final String FRAGMENT = "Fragment";
    private static String[] TAB_ORDER = new String[]{CURRENCY, DIVINATION_CARD, MAP, ESSENCE, FRAGMENT};

    private static final int DELAY = 17;
    private static final String IDLE = "idle";
    private static final String WORKING = "working..";
    private String status = IDLE;
    private static final boolean DEBUG = true;

    private static final int INVENTORY_ROWS = 5;
    private static final int INVENTORY_COLS = 12;
    private static final int INVENTORY_LEFTMOST_SLOT_X = 1297;
    private static final int INVENTORY_LEFTMOST_SLOT_Y = 615;
    private static final int INVENTORY_SLOT_SIZE = 52;
    private static final int STASH_TABS_Y = 144;
    private static final int STASH_TAB_SIZE = 60;

    private Integer TAB1 = STASH_TAB_SIZE * 1;
    private Integer TAB2 = STASH_TAB_SIZE * 2;
    private Integer TAB3 = STASH_TAB_SIZE * 3;
    private Integer TAB4 = STASH_TAB_SIZE * 4;
    private Integer TAB5 = STASH_TAB_SIZE * 5;

    private Map<String, Integer> STASH_TAB_X_FOR_RARITY = ImmutableMap.of(
            CURRENCY, TAB1,
            DIVINATION_CARD, TAB2,
            MAP, TAB3,
            ESSENCE, TAB4,
            FRAGMENT, TAB5);

    private static final Pattern NEWLINE = Pattern.compile("\\r?\\n");



    private Robot robot;
    private GlobalKeyboardHook globalKeyboardHook;
    private ImageTemplateMatcher imageTemplateMatcher;


    public Main() throws AWTException, UnsupportedFlavorException, IOException {

        imageTemplateMatcher = new ImageTemplateMatcher();
        robot = new Robot();
        globalKeyboardHook = new GlobalKeyboardHook(true);
        robot.setAutoDelay(DELAY);
        addEventListeners();

        try {
            while (run) {
//                Thread.sleep(1);
            }
        }
//        catch (InterruptedException e) {
        //Do nothing
        finally {
            System.out.println("shutting down..");
            globalKeyboardHook.shutdownHook();
        }
    }

    private void addEventListeners() {

        globalKeyboardHook.addKeyListener(new GlobalKeyAdapter() {

            @Override
            public void keyReleased(GlobalKeyEvent event) {

                if (CTRL_X(event)) {
                    robotIsWorking = false;
                    System.out.println("action cancelled");
                }

                if (CTRL_D(event)) {
                    analyzeThenDumpInventoryToStash();
                }


                if (CTRL_R(event)) {

                    BufferedImage img = robot.createScreenCapture(new Rectangle(1264, 582, 646, 275));

                    try {
                        ImageIO.write(img, "png", new File("./inventory.png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageTemplateMatcher.findChaosOrbsInImage(img);

                }

                if (DEBUG) {
                    moveMouseCursorWithArrowKeys(event);

                }

            }
        });

    }

    private void itemIdentification() {
        System.out.format("ITEM: %s%n", identifyItem());
    }


    private void analyzeThenDumpInventoryToStash() {
        robotIsWorking = true;
        robot.mouseMove(1364, 507); //inventory safe spot

        new Thread(() -> {
            while (robotIsWorking) {
                robot.setAutoDelay(9);
                int x = INVENTORY_LEFTMOST_SLOT_X;
                int y = INVENTORY_LEFTMOST_SLOT_Y;

                String[][] inventoryGrid = new String[INVENTORY_ROWS][INVENTORY_COLS];
                Map<Point, String> itemInSlot = new HashMap<>();

                System.out.println("reading inventory..");

                OUTER:
                for (int row = 0; row < INVENTORY_ROWS; row++) {

                    if (!robotIsWorking) {
                        System.out.println("action cancelled.");
                        break OUTER;
                    }

                    for (int col = 0; col < INVENTORY_COLS; col++) {

                        if (!robotIsWorking) {
                            System.out.println("action cancelled.");
                            break OUTER;
                        }


                        robot.mouseMove(x, y);
                        String item = identifyItem();

                        if (item != null) {

                            itemInSlot.put(new Point(x, y), item);
                            inventoryGrid[row][col] = item;
                        } else {
                            inventoryGrid[row][col] = "EMPTY";
                        }


                        x += INVENTORY_SLOT_SIZE;
                    }
                    x = INVENTORY_LEFTMOST_SLOT_X;
                    y += INVENTORY_SLOT_SIZE;
                }
                System.out.println("done.");

                robotIsWorking = false;
                printGrid(inventoryGrid);
                robot.setAutoDelay(DELAY);
                smartDump(itemInSlot);

            }
        }).start();


    }

    private void smartDump(Map<Point, String> itemInSlot) {

        Map<Point, String> currencies = splitInventoryByType(CURRENCY, itemInSlot);
        Map<Point, String> div_cards = splitInventoryByType(DIVINATION_CARD, itemInSlot);
        Map<Point, String> maps = splitInventoryByType(MAP, itemInSlot);
        Map<Point, String> essences = splitInventoryByType(ESSENCE, itemInSlot);
        Map<Point, String> fragments = splitInventoryByType(FRAGMENT, itemInSlot);

        List<Map<Point, String>> inventories = new ArrayList<>();
        inventories.add(currencies);
        inventories.add(div_cards);
        inventories.add(maps);
        inventories.add(essences);
        inventories.add(fragments);

        for (Map<Point, String> inventory : inventories) {
            if (inventory.size() > 0) {
                dump(inventory);
            }
        }
    }

    private Map<Point, String> splitInventoryByType(String type, Map<Point, String> itemInSlot) {
        return itemInSlot.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(type))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void dump(Map<Point, String> itemInSlot) {
        robotIsWorking = true;

        String tab = itemInSlot.entrySet().iterator().next().getValue();
        System.out.format("dumping to %s%n", tab);
        int tabX = STASH_TAB_X_FOR_RARITY.get(tab);
        int tabY = STASH_TABS_Y;

        mouseClickXY(tabX, tabY);

        for (Map.Entry entry : itemInSlot.entrySet()) {

            Point p = (Point) entry.getKey();
            int itemX = (int) p.getX();
            int itemY = (int) p.getY();

            mouseCtrlClickXY(itemX, itemY);

        }

        robotIsWorking = false;
    }

    private void mouseCtrlClickXY(int x, int y) {
        robot.mouseMove(x, y);
        ctrl_click();
    }

    private void mouseClickXY(int x, int y) {
        robot.mouseMove(x, y);
        left_click();
    }

    private void printGrid(String[][] inventoryGrid) {
        for (String[] arr : inventoryGrid) {
            for (String el : arr) {
                System.out.print(el + " ");
            }
            System.out.println();
        }
    }

    public void blindDump() {

        robotIsWorking = true;
        new Thread(() -> {
            while (robotIsWorking) {

                int x = INVENTORY_LEFTMOST_SLOT_X;
                int y = INVENTORY_LEFTMOST_SLOT_Y;
                System.out.println("dumping..");
                OUTER:
                for (int row = 0; row < INVENTORY_ROWS; row++) {

                    if (!robotIsWorking) {
                        System.out.println("action cancelled.");
                        break OUTER;
                    }

                    for (int col = 0; col < INVENTORY_COLS; col++) {

                        if (!robotIsWorking) {
                            System.out.println("action cancelled.");
                            break OUTER;
                        }

                        mouseCtrlClickXY(x, y);
                        x += INVENTORY_SLOT_SIZE;
                    }
                    x = INVENTORY_LEFTMOST_SLOT_X;
                    y += INVENTORY_SLOT_SIZE;
                }
                System.out.println("done.");
                robotIsWorking = false;

            }
        }).start();

    }

    private String identifyItem() {
        copyToClipboard();
        String clip = null;
        try {
            clip = getClipboardText();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        }
        resetClipboard();
        return getItem(clip);
    }

    private String getItem(String clip) {
        System.out.println(clip);

        String[] clipParts = newlineSplit(clip);
        String[] dashParts = clip.split("--------");

        String rarity = null;
        try {
            //if item is currency
            rarity = clipParts[0].split("Rarity: ")[1];

            if (clipParts[1].contains(MAP) || rarity.equals("Unique") && dashParts[1].contains(MAP)) {
                rarity = MAP;
            }

            if (clipParts[1].contains(ESSENCE) || clipParts[1].contains("Remnant of Corruption")) {
                rarity = ESSENCE;
            }

            if (clipParts[1].contains("Offering to the Goddess") || clipParts[1].contains("Sacrifice at ") || clipParts[1].contains("Splinter of ")
                    || clipParts[1].contains("Fragment of the ") || (rarity.equals("Normal") && clipParts[1].contains("Mortal "))
                    || clipParts[1].contains(" Scarab") || clipParts[1].contains("Divine Vessel")) {
                rarity = FRAGMENT;
            }

        } catch (ArrayIndexOutOfBoundsException e) {

        }
        return rarity;
    }

    private String[] newlineSplit(String clipboard) {
        return NEWLINE.split(clipboard);
    }

    public String getClipboardText() throws IOException, UnsupportedFlavorException {
        String clip = null;
        try {
            clip = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new NullPointerException("Clipboard was not a string!");
        }
        return clip;
    }

    private void resetClipboard() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
    }

    private void copyToClipboard() {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private void moveMouseCursorWithArrowKeys(GlobalKeyEvent event) {
        if (UP(event)) {
            move(0, -1);
        }
        if (DOWN(event)) {
            move(0, 1);
        }
        if (LEFT(event)) {
            move(-1, 0);
        }
        if (RIGHT(event)) {
            move(1, 0);
        }

    }

    private void move(int x_inc, int y_inc) {
        double y = MouseInfo.getPointerInfo().getLocation().getY();
        double x = MouseInfo.getPointerInfo().getLocation().getX();
        robot.mouseMove((int) x + x_inc, (int) y + y_inc);
        Point p = MouseInfo.getPointerInfo().getLocation();
        System.out.format("x: %s y: %s%n", p.getX(), p.getY());
    }

    private void escape() {
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);

    }

    private void right_click() {
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);

    }

    private void left_click() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    private void ctrl_click() {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private boolean CTRL_X(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_X && event.isControlPressed();
    }

    private boolean CTRL_D(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_D && event.isControlPressed() && !robotIsWorking;
    }


    private boolean CTRL_SHIFT_X(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_X && event.isControlPressed() && event.isShiftPressed();
    }

    private boolean CTRL_R(GlobalKeyEvent event) {
        return event.getVirtualKeyCode()
                == GlobalKeyEvent.VK_R && event.isControlPressed();
    }
//
//    private boolean CTRL_C(GlobalKeyEvent event) {
//        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_C && event.isControlPressed();
//    }

    private boolean UP(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_UP;
    }

    private boolean DOWN(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_DOWN;
    }

    private boolean LEFT(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_LEFT;
    }

    private boolean RIGHT(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_RIGHT;
    }

    public static void main(String[] args) throws AWTException, UnsupportedFlavorException, IOException {
        Main main = new Main();



        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook is running");
            main.globalKeyboardHook.shutdownHook();
        }));

    }
}

