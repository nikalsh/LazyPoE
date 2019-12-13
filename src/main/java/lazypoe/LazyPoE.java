package lazypoe;

import jdk.nashorn.internal.objects.Global;
import lazypoe.ComputerVision.ImageTemplateMatcher;
import lazypoe.GUI.GUI;
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
    private static final boolean DEBUG = false;
    private int CLICK_DELAY = 18;
    private int SCREENSHOT_DELAY = 125;
    private int CLIPBOARD_DELAY = 15;

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

    private Map<String, Integer> STASH_TAB_X_FOR_RARITY = new HashMap<>();

    private List<GUI.ButtonSlot> protectedSlots = null;
    private Point TPSlot;
    private static final Point PORTAL_LOC = new Point(952, 428);

    private static final Pattern NEWLINE = Pattern.compile("\\r?\\n");

    private Robot robot;
    private GlobalKeyboardHook globalKeyboardHook;
    private ImageTemplateMatcher imageTemplateMatcher;

    public LazyPoE() throws AWTException, UnsupportedFlavorException, IOException {
        robot = new Robot();
        imageTemplateMatcher = new ImageTemplateMatcher();
        robot.setAutoDelay(CLICK_DELAY);
        initInventory();
        initGlobalKeyboardHook();
        status = IDLE;
    }

    private void initGlobalKeyboardHook() {
        globalKeyboardHook = new GlobalKeyboardHook(true);
        addEventListeners();
    }

    private void addEventListeners() {

        globalKeyboardHook.addKeyListener(new GlobalKeyAdapter() {

            @Override
            public void keyReleased(GlobalKeyEvent event) {
                if (Utils.PoEIsOpen()) {

                    if (CTRL_X(event)) {
                        status = WORKING;
                        analyzeThenDumpInventoryToStash();
                        status = IDLE;
                        disable();
                        enable();
                    }

                    if (CTRL_R(event)) {
                        status = WORKING;
                        autoTP();
                        status = IDLE;
                        disable();
                        enable();
                    }

                    if (CTRL_F(event)) {
                        status = WORKING;
                        TP();
                        status = IDLE;
                        disable();
                        enable();
                    }

                    if (F1(event)) {
                        disable();
                        enable();
                    }

                    if (DEBUG) {
                        moveMouseCursorWithArrowKeys(event);
                    }

                }
            }

        });

    }

    public void setCLICK_DELAY(int CLICK_DELAY) {
        this.CLICK_DELAY = CLICK_DELAY;
    }

    public void setSCREENSHOT_DELAY(int SCREENSHOT_DELAY) {
        this.SCREENSHOT_DELAY = SCREENSHOT_DELAY;
    }

    public void setCLIPBOARD_DELAY(int CLIPBOARD_DELAY) {
        this.CLIPBOARD_DELAY = CLIPBOARD_DELAY;
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


    private BufferedImage getStashTabScreenshot() {
        robot.setAutoDelay(125);
        BufferedImage image = robot.createScreenCapture(new Rectangle(10, 161, 637, 710));
        robot.setAutoDelay(CLICK_DELAY);
        return image;
    }


    private void autoTP() {
//        System.out.println("auto tp");
        status = "auto tp..";
        if (!stashIsOpen()) {
            openEnterPortal();
        }
    }

    private void TP() {
//        System.out.println("tp");
        status = "opening portal";
        if (!stashIsOpen()) {
            openPortal();
        }
    }

    private void openEnterPortal() {

        if (!stashIsOpen()) {
            openPortal();
            enterPortal();
        }
    }

    private void openPortal() {
        robot.setAutoDelay(50);
        if (DEBUG) {
            System.out.println(TPSlot);
        }

        if (!inventoryIsOpen()) {
            toggleInventory();
        }

        mouseRightClickXY((int) TPSlot.getX(), (int) TPSlot.getY());
        toggleInventory();

        robot.setAutoDelay(CLICK_DELAY);
    }

    private void enterPortal() {
        mouseClickXY((int) PORTAL_LOC.getX(), (int) PORTAL_LOC.getY());
    }

    private boolean inventoryIsOpen() {
        BufferedImage img = robot.createScreenCapture(new Rectangle(1264, 0, 646, 110));
//        Utils.BItoFile(img, "inventoryz");
        return imageTemplateMatcher.inventory(img);
    }

    private boolean stashIsOpen() {
        BufferedImage img = robot.createScreenCapture(new Rectangle(125, 0, 390, 125));
//        Utils.BItoFile(img, "stashZ");
        return imageTemplateMatcher.stash(img);
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
                imageTemplateMatcher.findOrbs(img);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        t.start();

    }

    private void realTimeTabAnalysis() {
        Thread t = new Thread(() -> {
            robotIsWorking = true;

            while (robotIsWorking) {
                BufferedImage img = getStashTabScreenshot();
                System.out.println(imageTemplateMatcher.getNameOfOpenTab(img));

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

        if (stashIsOpen()) {

            robotIsWorking = true;
//        robot.mouseMove(1364, 507); //inventory safe spot
            robot.setAutoDelay(CLIPBOARD_DELAY);

            Map<Point, String> itemInSlot = new HashMap<>();

            status = "analyzing inventory";

            int x, y;
            for (Point slot : inventorySlots) {
                x = (int) slot.getX();
                y = (int) slot.getY();

                robot.mouseMove(x, y);

                String item = identifyItem();
                if (DEBUG) {
                    System.out.println(item);
                }
                if (item != null) {
                    itemInSlot.put(slot, item);
                }
            }

            if (DEBUG) {
                System.out.println("done.");
            }
            status = "done";
            robotIsWorking = false;
            robot.setAutoDelay(CLICK_DELAY);

            smartDump(itemInSlot);

        }
    }

    private void smartDump(Map<Point, String> itemInSlot) {

        Map<Point, String> currencies = splitInventoryByType(CURRENCY, itemInSlot);
        Map<Point, String> div_cards = splitInventoryByType(DIVINATION_CARD, itemInSlot);
        Map<Point, String> maps = splitInventoryByType(MAP, itemInSlot);
        Map<Point, String> essences = splitInventoryByType(ESSENCE, itemInSlot);
        Map<Point, String> fragments = splitInventoryByType(FRAGMENT, itemInSlot);
        Map<Point, String> fossils = splitInventoryByType(FOSSIL, itemInSlot);

        Map<String, Map<Point, String>> inventories = new HashMap<>();
        inventories.put(CURRENCY, currencies);
        inventories.put(DIVINATION_CARD, div_cards);
        inventories.put(MAP, maps);
        inventories.put(ESSENCE, essences);
        inventories.put(FRAGMENT, fragments);
        inventories.put(FOSSIL, fossils);

        openFirstTab();

        for (int i = 0; i < 6; i++) {

            robot.setAutoDelay(SCREENSHOT_DELAY);

            BufferedImage bi = getStashTabScreenshot();
            String openTab = imageTemplateMatcher.getNameOfOpenTab(bi);

            if (openTab.equals("Misc")) {
                try {
                    ImageIO.write(bi, "png", new File("miscs/Misc" + System.currentTimeMillis() + ".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Map<Point, String> inventory = inventories.get(openTab);

//            System.out.println(openTab);

            if (inventory != null) {

                if (inventory.size() > 0) {
                    robot.setAutoDelay(CLICK_DELAY);
                    dump(inventory);
                }

            }
            robot.setAutoDelay(SCREENSHOT_DELAY);

            openNextTab();
        }
        robot.setAutoDelay(CLICK_DELAY);

    }

    private void openNextTab() {
        robot.keyPress(KeyEvent.VK_RIGHT);
        robot.keyRelease(KeyEvent.VK_RIGHT);
    }

    private Map<Point, String> splitInventoryByType(String type, Map<Point, String> itemInSlot) {
        return itemInSlot.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(type))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void openFirstTab() {
        int x = 639;
        int y = 141;

        robot.setAutoDelay(25);
        mouseClickXY(x, y);
        upPress();
        enterPress();
        mouseClickXY(x, y);
        robot.setAutoDelay(CLICK_DELAY);

    }

    private void dump(Map<Point, String> itemInSlot) {
        robotIsWorking = true;
        String tab = itemInSlot.entrySet().iterator().next().getValue();

        StringBuilder sb = new StringBuilder("dumping ").append(tab);
        status = sb.toString();

        if (DEBUG) {
        System.out.format("dumping to %s%n", tab);
        }

        for (Map.Entry entry : itemInSlot.entrySet()) {

            Point p = (Point) entry.getKey();
            int itemX = (int) p.getX();
            int itemY = (int) p.getY();

            mouseCtrlClickXY(itemX, itemY);
        }

        robotIsWorking = false;
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
        clip = getClipboardText();
        resetClipboard();
        return getItem(clip);
    }

    private String getItem(String clip) {

        if (DEBUG) {
            System.out.println(clip);
        }

        String[] clipParts = newlineSplit(clip);
        String[] dashParts = clip.split("--------");

        String rarity = null;
        try {

            rarity = clipParts[0].split("Rarity: ")[1]; /* == Currency */

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

    public String getClipboardText() {
        String clip = null;
        try {
            clip = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            System.out.println("Clpiboard was not a string");
            clip = "";
        }
        return clip;
    }

    private void resetClipboard() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
    }

    private void setClipboard(String s) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
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

    public void disable() {
        globalKeyboardHook.shutdownHook();
    }

    public void enable() {
        initGlobalKeyboardHook();
    }

    public boolean isEnabled() {
        return globalKeyboardHook.isAlive();
    }

    private void mouseCtrlClickXY(int x, int y) {
        robot.mouseMove(x, y);
        ctrlClick();
    }

    private void mouseClickXY(int x, int y) {
        robot.mouseMove(x, y);
        leftClick();
    }

    private void mouseRightClickXY(int x, int y) {
        robot.mouseMove(x, y);
        rightClick();
    }

    private void escape() {
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
    }

    private void upPress() {
        robot.keyPress(KeyEvent.VK_UP);
        robot.keyRelease(KeyEvent.VK_UP);
    }

    private void enterPress() {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    private void rightClick() {
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }

    private void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    private void ctrlClick() {
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

    private boolean W(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_W;
    }

    private boolean D(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_D;
    }

    private boolean CTRL_SHIFT_R(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_R && event.isControlPressed() && event.isShiftPressed();
    }

    private boolean CTRL_F(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_F && event.isControlPressed();
    }

    private boolean CTRL_R(GlobalKeyEvent event) {
        return event.getVirtualKeyCode()
                == GlobalKeyEvent.VK_R && event.isControlPressed();
    }

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

    private boolean F1(GlobalKeyEvent event) {
        return event.getVirtualKeyCode() == GlobalKeyEvent.VK_F1;
    }

}
