package lazypoe;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageTemplateMatcher {

    private File chaosOrbPath = new File("./assets/chaos_orb.jpg");
    private BufferedImage chaosOrb = null;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public ImageTemplateMatcher() {
        loadTemplates();

    }

    private void loadTemplates() {

        try {
            chaosOrb = ImageIO.read(chaosOrbPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Mat bufferedImageToMat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public void findChaosOrbsInImage(BufferedImage screenshot) {

        Mat source = null;
        Mat template = Imgcodecs.imread("assets/chaos_orb_one.png");
        Mat output = new Mat();
        Mat grayTemplate = new Mat();


        output.create(template.rows(), template.cols(), CvType.CV_32FC1);

        System.out.println(template.channels());
        Imgproc.cvtColor(template, grayTemplate, Imgproc.COLOR_RGB2GRAY);

        try {
            source = bufferedImageToMat(screenshot);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int matchMethod = Imgproc.TM_CCOEFF_NORMED;
//            int matchMethod = Imgproc.TM_SQDIFF_NORMED;
        Imgproc.matchTemplate(source, template, output, matchMethod);
        Imgcodecs.imwrite("gray.png", grayTemplate);
        Imgcodecs.imwrite("output.png", output);


        double threshold = 0.75;
        Core.MinMaxLocResult mmr = null;
        Point matchLoc = null;

//        Point minLoc = mmr.minLoc;

        int orbCount = 0;
        long t0 = System.currentTimeMillis();
        while (true) {


            mmr = Core.minMaxLoc(output);
            matchLoc = mmr.maxLoc;


            if (mmr.maxVal > threshold) {


                Imgproc.rectangle(source, matchLoc, new Point(matchLoc.x + template.cols(),
                        matchLoc.y + template.rows()), new Scalar(0, 255, 0), 1);

                Imgproc.rectangle(output, matchLoc,
                        new Point(matchLoc.x + template.cols(), matchLoc.y + template.rows()),
                        new Scalar(0, 0, 255), -1);

                orbCount++;
            } else {

                long time = System.currentTimeMillis() - t0;
                System.out.println("completed in " + time + " ms");
                break;
            }
        }

        System.out.println("found " + orbCount + " orbs");
        Imgcodecs.imwrite("./matchedChaos.png", source);

    }

//    while(true)
//    {
//        mmr = Core.minMaxLoc(result);
//        matchLoc = mmr.maxLoc;
//        if(mmr.maxVal >=0.9)
//        {
//            Core.rectangle(img, matchLoc,
//                    new Point(matchLoc.x + templ.cols(),matchLoc.y + templ.rows()),
//                    new    Scalar(0,255,0));
//            Core.rectangle(result, matchLoc,
//                    new Point(matchLoc.x + templ.cols(),matchLoc.y + templ.rows()),
//                    new    Scalar(0,255,0),-1);
//            //break;
//        }
//        else
//        {
//            break; //No more results within tolerance, break search
//        }
//    }

}