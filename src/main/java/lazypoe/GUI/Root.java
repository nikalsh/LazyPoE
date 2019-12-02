package lazypoe.GUI;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Root {
    private StackPane root;
    private Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;


    public StackPane getRoot() {
        return root;
    }

    public Root(Stage primaryStage) {
        this.primaryStage = primaryStage;
        root = new StackPane();
        root.setId("ROOTNODE");
        setEventListeners();

    }

    private void setEventListeners() {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
    }


}
