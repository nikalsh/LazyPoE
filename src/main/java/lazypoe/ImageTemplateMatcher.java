package lazypoe;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageTemplateMatcher {

    private String chaosOrbPath = "assets/chaos_asdorb.jpg";
    private String exaltedOrbPath = "assets/exalted_orb1.jpg";
    private String alchemyOrbPath = "assets/alchemy_orb.jpg";

    private String currencyTabPath = "assets/currency_tab.jpg";
    private String divTabPath = "assets/div_tab.jpg";
    private String essenceTabPath = "assets/essence_tab.jpg";
    private String mapTabPath = "assets/map_tab.jpg";
    private String fragmentTabPath = "assets/fragment_tab.jpg";
    private String delveTabPath = "assets/delve_tab.jpg";

    private String inventoryPath = "assets/inventory.jpg";
    private Template inventoryTemplate = new Template("Inventory", "green", inventoryPath, 0.9);


    private Template currencyTabTemplate = new Template("Currency", "green", currencyTabPath, 0.85);
    private Template divTabTemplate = new Template("Divination Card", "green", divTabPath, 0.85);
    private Template essenceTabTemplate = new Template("Essence", "green", essenceTabPath, 0.85);
    private Template mapTabTemplate = new Template("Map", "green", mapTabPath, 0.85);
    private Template fragmentTabTemplate = new Template("Fragment", "green", fragmentTabPath, 0.85);
    private Template delveTabTemplate = new Template("Fossil", "green", delveTabPath, 0.85);

    private List<Template> tabTemplates = new ArrayList<>();


    private Template chaosOrbTemplate = new Template("Chaos", "green", chaosOrbPath, 0.825);
    private Template exaltedOrbTemplate = new Template("Exalt", "red", exaltedOrbPath, 0.85);
    private Template alchemyOrbTemplate = new Template("Alch", "blue", alchemyOrbPath, 0.9);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ImageTemplateMatcher() {
        loadTemplates();
    }

    private void loadTemplates() {
        tabTemplates.add(currencyTabTemplate);
        tabTemplates.add(divTabTemplate);
        tabTemplates.add(essenceTabTemplate);
        tabTemplates.add(mapTabTemplate);
        tabTemplates.add(fragmentTabTemplate);
        tabTemplates.add(delveTabTemplate);
    }

    public boolean isInventoryOpen(BufferedImage img) {
        return matchTemplateWithBI(inventoryTemplate, Utils.bufferedImageToMat(img), inventoryTemplate.getThreshold()).getCount() > 0;
    }

    public String getNameOfOpenTab(BufferedImage img) {
        Mat s1 = null;
        TemplateMatchingResult res = null;
        for (int i = 0; i < tabTemplates.size(); i++) {

            if (i == 0) {
                res = matchTemplateWithBI(tabTemplates.get(0), Utils.bufferedImageToMat(img), tabTemplates.get(0).getThreshold());

                if (res.getCount() > 0) {
                    return tabTemplates.get(i).getName();
                }

                s1 = res.getMat();

            } else {
                res = matchTemplateWithBI(tabTemplates.get(i), s1, tabTemplates.get(i).getThreshold());

                if (res.getCount() > 0) {
                    return tabTemplates.get(i).getName();
                }

                s1 = res.getMat();

            }
        }

        return "Misc";
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

        int matchMethod = Imgproc.TM_CCOEFF_NORMED;

        Imgproc.matchTemplate(sourceGray, grayTemplate, output, matchMethod);

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

                break;
            }
        }
/*
uncomment below for debugging
 */
//        System.out.println("found " + orbCount + " " + template.getName());
//        Imgcodecs.imwrite("./matched" + template.getName() + ".png", sourceRGB);
        try {
            return new TemplateMatchingResult(orbCount, sourceRGB);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getLowestThreshold(BufferedImage screenshot, Template template, int actualOrbCount) {

        int orbCount = 0;
        double threshold = 0.9;
        orbCount = match(screenshot, threshold, template);

        while (orbCount < actualOrbCount) {

            threshold -= 0.001;
            orbCount = match(screenshot, threshold, template);
        }

        System.out.println(threshold);
    }

    public Integer match(BufferedImage screenshot, double threshhold, Template template) {
        System.out.println("trying threshold " + threshhold);
        return matchTemplateWithBI(template, Utils.bufferedImageToMat(screenshot), threshhold).getCount();
    }

    public boolean findTab(BufferedImage img) {

        Mat s1 = matchTemplateWithBI(currencyTabTemplate, Utils.bufferedImageToMat(img), currencyTabTemplate.getThreshold()).getMat();
        Mat s2 = matchTemplateWithBI(divTabTemplate, s1, divTabTemplate.getThreshold()).getMat();
        Mat s3 = matchTemplateWithBI(delveTabTemplate, s2, delveTabTemplate.getThreshold()).getMat();
        Mat s4 = matchTemplateWithBI(essenceTabTemplate, s3, essenceTabTemplate.getThreshold()).getMat();
        Mat s5 = matchTemplateWithBI(fragmentTabTemplate, s4, fragmentTabTemplate.getThreshold()).getMat();
        Mat s6 = matchTemplateWithBI(mapTabTemplate, s5, mapTabTemplate.getThreshold()).getMat();

        HighGui.namedWindow("tabs", HighGui.WINDOW_AUTOSIZE);
        HighGui.imshow("tabs", s6);
        HighGui.moveWindow("tabs", 400, 400);
        HighGui.waitKey(1);

        return true;
    }

    public void findOrbs(BufferedImage screenshot) {

        BImatchAll(screenshot);
    }

    public void BImatchAll(BufferedImage s) {
        matchAllOrbTemplates(Utils.bufferedImageToMat(s));
    }

    public void matchAllOrbTemplates(Mat mat) {
        Mat s1 = matchTemplateWithBI(chaosOrbTemplate, mat, chaosOrbTemplate.getThreshold()).getMat();
        Mat s2 = matchTemplateWithBI(exaltedOrbTemplate, s1, exaltedOrbTemplate.getThreshold()).getMat();
        Mat s3 = matchTemplateWithBI(alchemyOrbTemplate, s2, alchemyOrbTemplate.getThreshold()).getMat();

        HighGui.namedWindow("orbs", HighGui.WINDOW_AUTOSIZE);
        HighGui.imshow("orbs", s3);
        HighGui.moveWindow("orbs", 400, 400);
        HighGui.waitKey(1);
    }


}