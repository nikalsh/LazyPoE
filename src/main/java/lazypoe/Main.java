package lazypoe;

        import javafx.application.Application;
        import lazypoe.GUI.GUI;
        import org.opencv.core.Core;

        import java.awt.AWTException;
        import java.awt.datatransfer.UnsupportedFlavorException;
        import java.io.IOException;
        import java.io.InputStream;
        import java.nio.file.Files;
        import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {



        Application.launch(GUI.class);
    }
}

