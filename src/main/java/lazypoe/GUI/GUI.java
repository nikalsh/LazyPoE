package lazypoe.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lazypoe.LazyPoE;

import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUI extends Application {

    public class ButtonSlot extends Button {

        private Point slot;
        private boolean protect;

        public boolean isProtect() {
            return protect;
        }

        public ButtonSlot(Point slot) {
            super();
            this.slot = slot;
            this.reset();

            this.setOnMousePressed(event -> {

                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    protect = true;
                    inventorySlots.stream()
                            .filter(e -> e.getText().equals("P"))
                            .forEach(j -> j.setText("X"));

                    this.setText("P");

                } else if (event.getButton().equals(MouseButton.PRIMARY)) {
                    protect = !protect;
                    this.setText(protect ? "X" : "O");

                }
                lazyPoE.setProtectedSlots(inventorySlots);
            });

        }

        public void reset() {
            int x = (int) slot.getX();
            int y = (int) slot.getY();

            if (x == 0 && y == 4) {
                setText("P");
                protect = true;
            } else if (x == 0 && y == 3) {
                setText("X");
                protect = true;
            } else {
                setText("O");
                protect = false;
            }
            lazyPoE.setProtectedSlots(inventorySlots);
        }

        public Point getSlot() {
            return slot;
        }

        public void setSlot(Point slot) {
            this.slot = slot;
        }
    }

    private Label screenshotDelayLabel;
    private Label clickDelayLabel;
    private Label clipboardDelayLabel;
    private Label statusLabel;
    private Button toggleButton;
    private Button resetButton;
    private Label hotkeyLabel;
    private Spinner<Integer> clipboardDelaySpinner;
    private Spinner<Integer> screenshotDelaySpinner;
    private Spinner<Integer> clickDelaySpinner;
    int CLICK_DELAY = 18;
    int CLIPBOARD_DELAY = 15;
    int SCREENSHOT_DELAY = 2000;
    private LazyPoE lazyPoE;

    {
        try {
            lazyPoE = new LazyPoE();
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean started = false;
    private List<ButtonSlot> inventorySlots;
    private List<HBox> rows;
    private Button help;


    @Override
    public void start(Stage primaryStage) throws Exception {
        inventorySlots = new ArrayList<>();
        rows = new ArrayList<>();
        int cols;

        VBox buttonGrid = new VBox();
        for (int rows = 0; rows < 5; rows++) {
            HBox hbox = hbox();
            this.rows.add(hbox);

            for (cols = 0; cols < 12; cols++) {
                ButtonSlot buttonSlot = new ButtonSlot(new Point(cols, rows));
                inventorySlots.add(buttonSlot);
                this.rows.get(rows).getChildren().add(buttonSlot);
            }

            buttonGrid.getChildren().add(this.rows.get(rows));
        }


        lazyPoE = new LazyPoE();
        lazyPoE.setProtectedSlots(inventorySlots);

        Root initRoot = new Root(primaryStage);
        StackPane pane = initRoot.getRoot();

        Scene scene = new Scene(pane);
        scene.setFill(Color.TRANSPARENT);

        hotkeyLabel = getLabel();
        hotkeyLabel.setText("hotkeys " + (lazyPoE.isEnabled() ? "enabled" : "disabled"));

        statusLabel = getLabel();
        statusLabel.setText("asd");

        toggleButton = new Button("Toggle hotkeys");
        resetButton = new Button("Reset");
        help = new Button("?");
        help.setTooltip(new Tooltip(String.format("Left click to toggle%nX = ignored%nO = not ignored%nRight click = Portal slot")));

        addEventListeners();

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.BASELINE_CENTER);
        hbox.getChildren().addAll(hotkeyLabel);

        VBox middleVBox = new VBox();
        middleVBox.getChildren().addAll(buttonGrid, hbox(resetButton, help));
        TitledPane middleTitledPane = new TitledPane();
        middleTitledPane.setOpacity(1);
        middleTitledPane.setText("Inventory");
        middleTitledPane.setContent(middleVBox);
        middleTitledPane.setAnimated(false);
        middleTitledPane.getStyleClass().add("inventory");
        Accordion middlePanel = new Accordion(middleTitledPane);
        middlePanel.expandedPaneProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                primaryStage.sizeToScene();
            });
        });


        clickDelayLabel = getLabel();
        clickDelayLabel.setText("Click delay");
        clipboardDelayLabel = getLabel();
        clipboardDelayLabel.setText("Clipboard delay");
        screenshotDelayLabel = getLabel();
        screenshotDelayLabel.setText("Screenshot delay");

        clickDelaySpinner = new Spinner<>();
        clipboardDelaySpinner = new Spinner<>();
        screenshotDelaySpinner = new Spinner<>();

        SpinnerValueFactory<Integer> clickDelayValues = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5000, CLICK_DELAY);
        SpinnerValueFactory<Integer> clipboardDelayValues = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5000, CLIPBOARD_DELAY);
        SpinnerValueFactory<Integer> screenshotDelayValues = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5000, SCREENSHOT_DELAY);

        clickDelaySpinner.setValueFactory(clickDelayValues);
        clipboardDelaySpinner.setValueFactory(clipboardDelayValues);
        screenshotDelaySpinner.setValueFactory(screenshotDelayValues);

        clickDelaySpinner.valueProperty().addListener((obs, oldValue, newValue) -> this.lazyPoE.setCLICK_DELAY(newValue));
        clipboardDelaySpinner.valueProperty().addListener((obs, oldValue, newValue) -> this.lazyPoE.setCLIPBOARD_DELAY(newValue));
        screenshotDelaySpinner.valueProperty().addListener((obs, oldValue, newValue) -> this.lazyPoE.setSCREENSHOT_DELAY(newValue));


        VBox bottomVBox = new VBox();

        VBox vbox1 = new VBox();
//q                hbox1(clickDelayLabel, clickDelaySpinner), hbox1(clipboardDelayLabel, clipboardDelaySpinner), hbox1(screenshotDelayLabel, screenshotDelaySpinner));

        bottomVBox.setPadding(new Insets(10, 0, 10, 0));
        bottomVBox.getChildren().addAll(vbox1);
        TitledPane bottomTitledPane = new TitledPane();
        bottomTitledPane.setOpacity(1);
        bottomTitledPane.setText("Options");
        bottomTitledPane.setContent(bottomVBox);
        bottomTitledPane.setAnimated(false);
        bottomTitledPane.getStyleClass().add("inventory");
        Accordion bottomPanel = new Accordion(bottomTitledPane);
        bottomPanel.expandedPaneProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                primaryStage.sizeToScene();
            });
        });

        VBox topPanel = new VBox();
        topPanel.setPadding(new Insets(10, 0, 10, 0));
        topPanel.setSpacing(8.0);
        topPanel.getChildren().add(hbox);
        topPanel.getChildren().addAll(hbox(statusLabel), hbox(toggleButton));

        VBox ROOT = new VBox(topPanel, middlePanel, bottomPanel);
        pane.getChildren().add(ROOT);
        init(primaryStage, scene);
        started = true;
        pollStatus();
    }

    private void addEventListeners() {

        toggleButton.setOnAction(event -> {
            boolean enabled = lazyPoE.isEnabled();
            if (enabled) {
                lazyPoE.disable();
            } else {
                lazyPoE.enable();
            }

            hotkeyLabel.setText("hotkeys " + (lazyPoE.isEnabled() ? "enabled" : "disabled"));

        });

        resetButton.setOnAction(event -> {
            inventorySlots.stream().forEach(ButtonSlot::reset);
        });


    }

    private void pollStatus() {
        Thread t = new Thread(() -> {
            while (started) {

                Platform.runLater(() -> statusLabel.setText(new StringBuilder("status: ").append(lazyPoE.getStatus()).toString()));
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();
    }

    private void init(Stage primaryStage, Scene scene) {
        primaryStage.setTitle("Hello World");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setOpacity(0.8);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.getScene().getStylesheets().setAll(getClass().getClassLoader().getResource("main.css").toString());
        primaryStage.show();
        pollStatus();
    }

    private Label getLabel() {
        Label label = new Label();
        label.setTextFill(Color.WHITESMOKE);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        return label;
    }

    private HBox hbox(Node... node) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(node);
        return hbox;
    }

    private HBox hbox1(Node... node) {
        HBox hbox = new HBox();
//        hbox.setPadding(new Insets(0, 10, 0, 10));
//        hbox.setSpacing(0.8);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.getChildren().addAll(node);
        return hbox;
    }

}
