package lazypoe;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

public class Template {

    private Mat matrix;
    private String name;
    private Scalar color;
    private String pathToFile;
    private double threshold;


    Template(String name, String color, String pathToFile, double threshold) {
        this.name = name;
        this.color = getScalar(color);
        this.pathToFile = pathToFile;
        this.threshold = threshold;
        matrix = Imgcodecs.imread(this.pathToFile);
    }

    public double getThreshold() {
        return threshold;
    }

    private Scalar getScalar(String color) {

        switch (color) {
            case "blue":
                return new Scalar(255, 0, 0);
            case "red":
                return new Scalar(0, 0, 255);
            case "green":
                return new Scalar(0, 255, 0);
            case "white":
                return new Scalar(255, 255, 255);
            case "black":
                return new Scalar(0, 0, 0);
        }
        return null;
    }

    public Mat getMat() {
        return matrix;
    }

    public String getName() {
        return name;
    }

    public Scalar getColor() {
        return color;
    }
}
