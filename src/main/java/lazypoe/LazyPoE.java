package lazypoe;

import lazypoe.GUI.GUI;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author nikalsh
 */

public class LazyPoE {

    private static final Dimension RESOLUTION = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Rectangle FULLSCREEN = new Rectangle(RESOLUTION);

    private String status = INIT;
    private static final String IDLE = "idle";
    private static final String INIT = "starting..";
    private static final String WORKING = "working..";

    private static boolean robotIsWorking = false;
    private static final boolean DEBUG = true;
    private static final int DELAY = 17;

    private static final String CURRENCY = "Currency";
    private static final String DIVINATION_CARD = "Divination Card";
    private static final String MAP = "Map";
    private static final String ESSENCE = "Essence";
    private static final String FRAGMENT = "Fragment";
    private static final String FOSSIL = "Fossil";
    private static final String MISC = "Misc";
    private static String[] TAB_ORDER = new String[]{CURRENCY, DIVINATION_CARD, MAP, ESSENCE, FRAGMENT, FOSSIL, MISC};
    private List<Point> inventorySlots;

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
    private Integer TAB6 = STASH_TAB_SIZE * 6;

    private Map<String, Integer> STASH_TAB_X_FOR_RARITY = new HashMap<>();

    private List<GUI.ButtonSlot> protectedSlots = null;
    private Point TPSlot;
    private static final Point PORTAL_LOC = new Point(952, 428);

    private static final Pattern NEWLINE = Pattern.compile("\\r?\\n");

    private Robot robot;
    private GlobalKeyboardHook globalKeyboardHook;
    private ImageTemplateMatcher imageTemplateMatcher;


    private void initTabMapping() {
        STASH_TAB_X_FOR_RARITY.put(CURRENCY, TAB1);
        STASH_TAB_X_FOR_RARITY.put(DIVINATION_CARD, TAB2);
        STASH_TAB_X_FOR_RARITY.put(MAP, TAB3);
        STASH_TAB_X_FOR_RARITY.put(ESSENCE, TAB4);
        STASH_TAB_X_FOR_RARITY.put(FRAGMENT, TAB5);
        STASH_TAB_X_FOR_RARITY.put(FOSSIL, TAB6);
    }

    public LazyPoE() throws AWTException, UnsupportedFlavorException, IOException {
        robot = new Robot();
        imageTemplateMatcher = new ImageTemplateMatcher();
        robot.setAutoDelay(DELAY);
        initTabMapping();
        initInventory();
        initGlobalKeyboardHook();
        status = IDLE;
    }

    public void setProtectedSlots(List<GUI.ButtonSlot> l) {
        this.protectedSlots = l;
        initInventory();
    }

    public void resetProtectedSlots() {
        this.protectedSlots = null;
        initInventory();
    }

    private void initInventory() {
        inventorySlots = new ArrayList<>();
        int x = INVENTORY_LEFTMOST_SLOT_X;
        int y = INVENTORY_LEFTMOST_SLOT_Y;

        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {

                if (!isProtected(col, row)) {
                    inventorySlots.add(new Point(x, y));
                }
                x += INVENTORY_SLOT_SIZE;
            }
            x = INVENTORY_LEFTMOST_SLOT_X;
            y += INVENTORY_SLOT_SIZE;
        }
    }

    private boolean isProtected(int col, int row) {

        if (protectedSlots == null) {
            if (col == 0 && row == 4) {
                return true;
            }
            if (col == 0 && row == 3) {
                return true;
            }

        } else {

            for (GUI.ButtonSlot bs : protectedSlots) {
                if (bs.getSlot().getX() == col && bs.getSlot().getY() == row) {
                    if (bs.getText().equals("P")) {
                        setPortalSlot(bs.getSlot());
                    }
                    return bs.isProtect();
                }

            }

        }
        return false;
    }

    private void setPortalSlot(Point p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        this.TPSlot = new Point((x * INVENTORY_SLOT_SIZE) + INVENTORY_LEFTMOST_SLOT_X, (y * INVENTORY_SLOT_SIZE) + INVENTORY_LEFTMOST_SLOT_Y);
    }

    private void initGlobalKeyboardHook() {
        globalKeyboardHook = new GlobalKeyboardHook(true);
        addEventListeners();
    }

    private void addEventListeners() {

        globalKeyboardHook.addKeyListener(new GlobalKeyAdapter() {

            @Override
            public void keyReleased(GlobalKeyEvent event) {

                if (CTRL_X(event)) {
                    robotIsWorking = false;
                    status = IDLE;
                    System.out.println("action cancelled");
                }

                if (CTRL_D(event)) {
//                    blindDump();
                    status = WORKING;
                    analyzeThenDumpInventoryToStash();
                    status = IDLE;
                }

                if (CTRL_R(event)) {


                    autoTP();


//                    imageTemplateMatcher.findChaosAndExalt(getInventoryScreenshot());
//                    realTimeInventoryAnalysis();

                    status = IDLE;
                }

                if (DEBUG) {
                    moveMouseCursorWithArrowKeys(event);
                }

            }
        });

    }

    private void autoTP() {
        status = WORKING;
        status = "auto tp..";

        openEnterPortal();
    }

    private void openEnterPortal() {

        if (isInventoryOpen()) {
            openPortal();

        } else {
            toggleInventory();
            openPortal();
        }

    }

    private boolean isInventoryOpen() {
        BufferedImage img = robot.createScreenCapture(new Rectangle(1264, 0, 646, 110));
        return imageTemplateMatcher.isInventoryOpen(img);
    }

    private void openPortal() {
        robot.setAutoDelay(50);
        System.out.println(TPSlot);

        mouseRightClickXY((int) TPSlot.getX(), (int) TPSlot.getY());

        toggleInventory();

        mouseClickXY((int) PORTAL_LOC.getX(), (int) PORTAL_LOC.getY());
        robot.setAutoDelay(DELAY);
    }

    private void toggleInventory() {
        robot.keyPress(KeyEvent.VK_B);
        robot.keyRelease(KeyEvent.VK_B);
    }

    private BufferedImage getInventoryScreenshot() {
        return robot.createScreenCapture(new Rectangle(1264, 582, 646, 275));
    }

    private void realTimeInventoryAnalysis() {
        Thread t = new Thread(() -> {
            robotIsWorking = true;
            while (robotIsWorking) {
                BufferedImage img = robot.createScreenCapture(new Rectangle(1264, 582, 646, 275));
                imageTemplateMatcher.findChaosAndExalt(img);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        t.start();
    }

    private void itemIdentification() {
        System.out.format("ITEM: %s%n", identifyItem());
    }

    public String getStatus() {
        return status;
    }

    public void blindDump() {
        robotIsWorking = true;

        robot.mouseMove(1364, 507);

        new Thread(() -> {
            while (robotIsWorking) {

                int x = INVENTORY_LEFTMOST_SLOT_X;
                int y = INVENTORY_LEFTMOST_SLOT_Y;
                System.out.println("dumping..");
                status = "dumping..";
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
                status = "done";
                robotIsWorking = false;

            }
        }).start();

    }

    private void analyzeThenDumpInventoryToStash() {

        robotIsWorking = true;
        robot.mouseMove(1364, 507); //inventory safe spot
        robot.setAutoDelay(12);

        Map<Point, String> itemInSlot = new HashMap<>();

        status = "analyzing inventory";

        int x, y;
        for (Point slot : inventorySlots) {
            x = (int) slot.getX();
            y = (int) slot.getY();

            robot.mouseMove(x, y);
            String item = identifyItem();

            if (item != null) {
                itemInSlot.put(slot, item);
            }
        }

        System.out.println("done.");
        status = "done";
        robotIsWorking = false;
        robot.setAutoDelay(DELAY);

        smartDump(itemInSlot);
    }

//    private void analyzeThenDumpInventoryToStash() {
//        robotIsWorking = true;
//        robot.mouseMove(1364, 507); //inventory safe spot
//
//        while (robotIsWorking) {
//            robot.setAutoDelay(10);
//            int x = INVENTORY_LEFTMOST_SLOT_X;
//            int y = INVENTORY_LEFTMOST_SLOT_Y;
//
//            String[][] inventoryGrid = new String[INVENTORY_ROWS][INVENTORY_COLS];
//            Map<Point, String> itemInSlot = new HashMap<>();
//
//            status = "analyzing inventory";
//            System.out.println("reading inventory..");
//
//            OUTER:
//            for (int row = 0; row < INVENTORY_ROWS; row++) {
//
//                if (!robotIsWorking) {
//                    System.out.println("action cancelled.");
//                    break OUTER;
//                }
//
//                for (int col = 0; col < INVENTORY_COLS; col++) {
//
//                    if (!robotIsWorking) {
//                        System.out.println("action cancelled.");
//                        break OUTER;
//                    }
//
//
//                    robot.mouseMove(x, y);
//                    String item = identifyItem();
//
//                    if (item != null) {
//
//                        itemInSlot.put(new Point(x, y), item);
//                        inventoryGrid[row][col] = item;
//                    } else {
//                        inventoryGrid[row][col] = "EMPTY";
//                    }
//
//
//                    x += INVENTORY_SLOT_SIZE;
//                }
//                x = INVENTORY_LEFTMOST_SLOT_X;
//                y += INVENTORY_SLOT_SIZE;
//            }
//            System.out.println("done.");
//            status = "done";
//            robotIsWorking = false;
//            printGrid(inventoryGrid);
//            robot.setAutoDelay(DELAY);
//
//            smartDump(itemInSlot);
//
//        }
//
//    }

    private void smartDump(Map<Point, String> itemInSlot) {

        Map<Point, String> currencies = splitInventoryByType(CURRENCY, itemInSlot);
        Map<Point, String> div_cards = splitInventoryByType(DIVINATION_CARD, itemInSlot);
        Map<Point, String> maps = splitInventoryByType(MAP, itemInSlot);
        Map<Point, String> essences = splitInventoryByType(ESSENCE, itemInSlot);
        Map<Point, String> fragments = splitInventoryByType(FRAGMENT, itemInSlot);
        Map<Point, String> fossils = splitInventoryByType(FOSSIL, itemInSlot);

        List<Map<Point, String>> inventories = new ArrayList<>();
        inventories.add(currencies);
        inventories.add(div_cards);
        inventories.add(maps);
        inventories.add(essences);
        inventories.add(fragments);
        inventories.add(fossils);

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
        StringBuilder sb = new StringBuilder("dumping ").append(tab);
        status = sb.toString();
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

    private void mouseRightClickXY(int x, int y) {
        robot.mouseMove(x, y);
        right_click();
    }

    private void printGrid(String[][] inventoryGrid) {
        for (String[] arr : inventoryGrid) {
            for (String el : arr) {
                System.out.print(el + " ");
            }
            System.out.println();
        }
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
//        System.out.println(clip);

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
                    || clipParts[1].contains(" Scarab") || clipParts[1].contains("Divine Vessel") || clipParts[1].contains(" Breachstone")) {
                rarity = FRAGMENT;
            }

            if (rarity.equals(CURRENCY) && (clipParts[1].contains(" Fossil") || clipParts[1].contains(" Resonator"))) {
                rarity = FOSSIL;
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
            e.printStackTrace();
            System.out.println("Clpiboard was not a string");
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
            incrementallyMove(0, -1);
        }
        if (DOWN(event)) {
            incrementallyMove(0, 1);
        }
        if (LEFT(event)) {
            incrementallyMove(-1, 0);
        }
        if (RIGHT(event)) {
            incrementallyMove(1, 0);
        }

    }

    private void incrementallyMove(int x_inc, int y_inc) {
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

    public void disable() {
        globalKeyboardHook.shutdownHook();
    }

    public void enable() {
        initGlobalKeyboardHook();
    }

    public boolean isEnabled() {
        return globalKeyboardHook.isAlive();
    }

}
