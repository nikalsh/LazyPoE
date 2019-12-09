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

        }

        public Point getSlot() {
            return slot;
        }

        public void setSlot(Point slot) {
            this.slot = slot;
        }
    }

    private Label hotkeyLabel;
    private Label statusLabel;
    private Button toggleButton;
    private Button saveButton;
    private Button resetButton;
    private LazyPoE lazyPoE;
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
        saveButton = new Button("Save");
        resetButton = new Button("Reset");
        help = new Button("?");
        help.setTooltip(new Tooltip(String.format("Left click to toggle%nX = ignored%nO = not ignored%nRight click = Portal slot")));

        addEventListeners();

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.BASELINE_CENTER);
        hbox.getChildren().addAll(hotkeyLabel);

        VBox bottomVBox = new VBox();
        bottomVBox.getChildren().addAll(buttonGrid, hbox(saveButton, resetButton, help));

        TitledPane titledPane = new TitledPane();
        titledPane.setOpacity(1);
        titledPane.setText("Inventory");
        titledPane.setContent(bottomVBox);
        titledPane.setAnimated(false);
        titledPane.getStyleClass().add("inventory");

        Accordion bottomPanel = new Accordion(titledPane);
        bottomPanel.expandedPaneProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("hello");
            Platform.runLater(() -> {
                primaryStage.sizeToScene();
            });
        });

        VBox topPanel = new VBox();
        topPanel.setPadding(new Insets(10, 0, 10, 0));
        topPanel.setSpacing(8.0);
        topPanel.getChildren().add(hbox);
        topPanel.getChildren().addAll(hbox(statusLabel), hbox(toggleButton));

        VBox ROOT = new VBox(topPanel, bottomPanel);
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

        saveButton.setOnAction(event -> {
            lazyPoE.setProtectedSlots(inventorySlots);
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

}
