package lazypoe.GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUI extends Application {

    private Label welcomeLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {


        Root initRoot = new Root(primaryStage);
        StackPane pane = initRoot.getRoot();

        Scene scene = new Scene(pane, 300, 150);
        scene.setFill(Color.TRANSPARENT);

        welcomeLabel = getLabel();
        welcomeLabel.setText("Welcome");

        addEventListeners();

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.BASELINE_CENTER);

        hbox.getChildren().addAll(welcomeLabel);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(8.0);

        vbox.getChildren().add(hbox);

        pane.getChildren().add(vbox);
        init(primaryStage, scene);
    }

    private void addEventListeners() {

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
    }

    private Label getLabel() {
        Label label = new Label();
        label.setTextFill(Color.WHITESMOKE);
        label.setFont(Font.font("Mono Sans", FontWeight.BOLD, 40));
        return label;
    }


    public static void main(String[] args) {
        launch(GUI.class);
    }
}
