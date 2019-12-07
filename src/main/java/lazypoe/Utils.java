package lazypoe;

import java.awt.*;

public class Utils {

    public static GraphicsDevice[] getAllGraphicsDevices() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    }
}
