package lazypoe;

import com.google.common.collect.ImmutableList;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ImageTemplateMatcher {

    private String chaosOrbPath = "assets/chaos_asdorb.jpg";
    private String exaltedOrbPath = "assets/exalted_orb1.jpg";
    private String alchemyOrbPath = "assets/alchemy_orb.jpg";

    private String inventoryPath = "assets/inventory.jpg";
    private Template inventoryTemplate = new Template("Inventory", "green", inventoryPath, 0.9);
    private Template chaosOrbTemplate = new Template("Chaos", "green", chaosOrbPath, 0.825);
    private Template exaltedOrbTemplate = new Template("Exalt", "red", exaltedOrbPath, 0.85);
    private Template alchemyOrbTemplate = new Template("Alch", "blue", alchemyOrbPath, 0.9);

    private List<Template> templates = ImmutableList.of(
            chaosOrbTemplate,
            exaltedOrbTemplate
//            alchemyOrbTemplate
    );
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public ImageTemplateMatcher() {
        loadTemplates();

    }

    private void loadTemplates() {


    }

    public static Mat bufferedImageToMat(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    static BufferedImage matToBufferedImage(Mat matrix) throws Exception {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[] = mob.toArray();

        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

    public void findChaosAndExalt(BufferedImage screenshot) {


//        getLowestThreshold(screenshot, chaosOrbTemplate, 60);


        BImatchAll(screenshot);
    }

    public void BImatchAll(BufferedImage s) {
        MATmatchAll(bufferedImageToMat(s));

    }

    public void getLowestThreshold(BufferedImage screenshot, Template template, int actualOrbCount) {

        int orbCount = 0;

        double threshold = 0.9;

        orbCount = doTheThing(screenshot, threshold, template);

        while (orbCount < actualOrbCount) {

            threshold -= 0.001;

            orbCount = doTheThing(screenshot, threshold, template);
        }

        System.out.println(threshold);
    }

    public Integer doTheThing(BufferedImage screenshot, double threshhold, Template template) {
        System.out.println("trying threshold " + threshhold);


        return matchTemplateWithBI(template, bufferedImageToMat(screenshot), threshhold).getCount();
    }


    public void findChaosOrbInImage(BufferedImage screenshot) {


    }

    public boolean isInventoryOpen(BufferedImage img) {
        return matchTemplateWithBI(inventoryTemplate, bufferedImageToMat(img), inventoryTemplate.getThreshold()).getCount() > 0;
    }


    public TemplateMatchingResult matchTemplateWithBI(Template template, Mat screenshot, double threshold) {

        Mat templateMat = template.getMat();
        Mat sourceRGB = screenshot;
        Mat sourceGray = null;
        Mat output = new Mat();
        Mat grayTemplate = new Mat();

        output.create(templateMat.rows(), templateMat.cols(), CvType.CV_8U);


        sourceGray = new Mat();
        Imgproc.cvtColor(sourceRGB, sourceGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(templateMat, grayTemplate, Imgproc.COLOR_RGB2GRAY);


        Imgcodecs.imwrite("inventory.png", sourceGray);

        Mat thresh = new Mat();

//        Imgproc.threshold(sourceGray, thresh, 127, 255, Imgproc.THRESH_BINARY_INV);


        //otsu
        Mat blur = new Mat();
//        Imgproc.GaussianBlur(sourceGray, blur, new Size(5, 5), 0);
//        Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);


//        int threshCode = Imgproc.THRESH_TRUNC;

//        Imgproc.threshold(sourceGray, thresh, 127, 255, threshCode);

        //Adaptive
//        Imgproc.adaptiveThreshold(sourceGray, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
//
        Mat threshTemplate = new Mat();

        //adaptive
//        Imgproc.adaptiveThreshold(grayTemplate, threshTemplate, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);


        blur = new Mat();
//        Imgproc.GaussianBlur(grayTemplate, blur, new Size(5, 5), 0);
//        Imgproc.threshold(blur, threshTemplate, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

//        Imgcodecs.imwrite("threshTemplate" + template.getName() + ".png", threshTemplate);
//
//        Imgcodecs.imwrite("thresh.png", thresh);


        int matchMethod = Imgproc.TM_CCOEFF_NORMED;
//        Imgproc.matchTemplate(sourceRGB, templateMat, output, matchMethod);
//        Imgcodecs.imwrite("inventory.png", sourceRGB);

//        sourceGray = thresh;
//        grayTemplate = threshTemplate;
        Imgproc.matchTemplate(sourceGray, grayTemplate, output, matchMethod);
//        Imgproc.matchTemplate()
        Imgcodecs.imwrite("gray.png", grayTemplate);
        Imgcodecs.imwrite("output.png", output);


        Core.MinMaxLocResult mmr = null;
        Point matchLoc = null;


        int orbCount = 0;
        long t0 = System.currentTimeMillis();


        while (true) {


            mmr = Core.minMaxLoc(output);
            matchLoc = mmr.maxLoc;


            if (mmr.maxVal > threshold) {


                Imgproc.rectangle(sourceRGB, matchLoc, new Point(matchLoc.x + templateMat.cols(),
                        matchLoc.y + templateMat.rows()), template.getColor(), 1);

                Imgproc.putText(sourceRGB, template.getName(), new Point(matchLoc.x, matchLoc.y + templateMat.cols() / 2), Core.FONT_HERSHEY_PLAIN,
                        0.9, new Scalar(255, 255, 255), 1
                );


                Imgproc.rectangle(output, matchLoc,
                        new Point(matchLoc.x + templateMat.cols(), matchLoc.y + templateMat.rows()),
                        new Scalar(0, 0, 255), -1);

                orbCount++;
            } else {

                long time = System.currentTimeMillis() - t0;
//                System.out.println("completed in " + time + " ms");
                break;
            }
        }

        System.out.println("found " + orbCount + " " + template.getName());
        Imgcodecs.imwrite("./matched" + template.getName() + ".png", sourceRGB);


        try {
            return new TemplateMatchingResult(orbCount, sourceRGB);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void MATmatchAll(Mat mat) {


        Mat s1 = matchTemplateWithBI(chaosOrbTemplate, mat, chaosOrbTemplate.getThreshold()).getMat();
        Mat s2 = matchTemplateWithBI(exaltedOrbTemplate, s1, exaltedOrbTemplate.getThreshold()).getMat();
        Mat s3 = matchTemplateWithBI(alchemyOrbTemplate, s2, alchemyOrbTemplate.getThreshold()).getMat();

        HighGui.namedWindow("orbs", HighGui.WINDOW_AUTOSIZE);
        HighGui.imshow("orbs", s3);
        HighGui.moveWindow("orbs", 400, 400);
        HighGui.waitKey(1);


    }


}