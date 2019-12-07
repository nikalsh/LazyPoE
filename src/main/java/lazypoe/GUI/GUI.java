//package lazypoe.GUI;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.StackPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//import lazypoe.Main;
//
//import java.awt.*;
//import java.awt.datatransfer.UnsupportedFlavorException;
//import java.io.IOException;
//
//public class GUI extends Application {
//
//    private Label welcomeLabel;
//    private Label statusLabel;
//    private Main main;
//
//    private String URL = "https://upload.wikimedia.org/wikipedia/commons/c/c4/Orange-Fruit-Pieces.jpg";
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//
//        new Thread(() -> {
//            try {
//                main = new Main();
//
//
//            } catch (AWTException e) {
//                e.printStackTrace();
//            } catch (UnsupportedFlavorException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//
//
//        Root initRoot = new Root(primaryStage);
//        StackPane pane = initRoot.getRoot();
//
//        Scene scene = new Scene(pane, 300, 150);
//        scene.setFill(Color.TRANSPARENT);
//
//        welcomeLabel = getLabel();
//        welcomeLabel.setText("Welcome");
//
//        statusLabel = getLabel();
//        statusLabel.setText("asd");
//
//        addEventListeners();
//
//        HBox hbox = new HBox();
//        hbox.setAlignment(Pos.BASELINE_CENTER);
//
//        hbox.getChildren().addAll(welcomeLabel);
//
//        VBox vbox = new VBox();
//        vbox.setPadding(new Insets(20));
//        vbox.setSpacing(8.0);
//
//        vbox.getChildren().add(hbox);
//        vbox.getChildren().add(hbox(statusLabel));
//
//        pane.getChildren().add(vbox);
//        init(primaryStage, scene);
//    }
//
//    private void addEventListeners() {
//
//    }
//
//    private void init(Stage primaryStage, Scene scene) {
//        primaryStage.setTitle("Hello World");
//        primaryStage.initStyle(StageStyle.TRANSPARENT);
//        primaryStage.setAlwaysOnTop(true);
//        primaryStage.setOpacity(0.8);
//        primaryStage.setResizable(false);
//        primaryStage.setScene(scene);
//        primaryStage.getScene().getStylesheets().setAll(getClass().getClassLoader().getResource("main.css").toString());
//        primaryStage.show();
//    }
//
//    private Label getLabel() {
//        Label label = new Label();
//        label.setTextFill(Color.WHITESMOKE);
//        label.setFont(Font.font("Mono Sans", FontWeight.BOLD, 40));
//        return label;
//    }
//
//    private HBox hbox(Node... node) {
//        HBox hbox = new HBox();
//        hbox.setAlignment(Pos.CENTER);
//        hbox.getChildren().addAll(node);
//        return hbox;
//    }
//
////    public static void main(String[] args) {
////        launch(GUI.class);
////    }
//}
