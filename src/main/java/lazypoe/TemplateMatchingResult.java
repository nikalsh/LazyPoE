package lazypoe;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

public class TemplateMatchingResult {
    private int count;
    private Mat source;

    public TemplateMatchingResult(int count, Mat source) {
        this.count = count;
        this.source = source;
    }

    public int getCount() {
        return count;
    }

    public Mat getMat() {
        return source;
    }
}
