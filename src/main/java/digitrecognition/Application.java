package digitrecognition;

import org.apache.log4j.BasicConfigurator;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import utils.RectComparator;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
//import org.bytedeco.javacpp.Loader;
//import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
//import static org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT;
//import org.bytedeco.javacpp.opencv_core.CvContour;
//import org.bytedeco.javacpp.opencv_core.CvMemStorage;
//import org.bytedeco.javacpp.opencv_core.CvRect;
//import org.bytedeco.javacpp.opencv_core.CvSeq;
//import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
//import org.bytedeco.javacpp.opencv_core.IplImage;
//import org.bytedeco.javacpp.opencv_core.Mat;
//import org.bytedeco.javacpp.opencv_core.Point;
//import org.bytedeco.javacpp.opencv_core.Rect;
//import org.bytedeco.javacpp.opencv_core.Scalar;
//import org.bytedeco.javacpp.opencv_core.Size;
//import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
//import static org.bytedeco.javacpp.opencv_core.cvGetSize;
//import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
//import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
//import org.bytedeco.javacpp.opencv_imgproc;
//import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_OTSU;
//import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;
//import static org.bytedeco.javacpp.opencv_core.bitwise_not;
//import static org.bytedeco.javacpp.opencv_core.copyMakeBorder;
//import static org.bytedeco.javacpp.opencv_core.cvCreateMemStorage;
//import static org.bytedeco.javacpp.opencv_core.cvPoint;
//import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
//import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_EXTERNAL;
//import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;
//import static org.bytedeco.javacpp.opencv_imgproc.cvFindContours;
//import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
//import static org.bytedeco.javacpp.opencv_imgproc.resize;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IAMax;
import org.nd4j.linalg.factory.Nd4j;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvLoadImage;


public class Application {

    private static MultiLayerNetwork restored;
    private final static String IMAGEPATH = "samples/sample18.jpg";
    private final static String[] DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    public Application() {
        try {
            //String pathtoexe = System.getProperty("user.dir");
            File net = new File( "data/minist-model.zip");
            restored = ModelSerializer.restoreMultiLayerNetwork(net);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static BufferedImage IplImageToBufferedImage(IplImage src) {
        OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter paintConverter = new Java2DFrameConverter();
        Frame frame = grabberConverter.convert(src);
        return paintConverter.getBufferedImage(frame, 1);
    }

    public static void displayImage(Mat imgage) {
        BufferedImage img = IplImageToBufferedImage(new IplImage(imgage));
        JFrame frame = new JFrame();
        frame.setTitle("Result");
        frame.setSize(new Dimension(img.getWidth() + 10, img.getHeight() + 10));
        frame.setLayout(new FlowLayout());
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(img));
        frame.add(label);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }



    public static void main(String[] args) throws IOException {

        BasicConfigurator.configure();

        Application app = new Application();


        /*Load image in grayscale mode*/
        IplImage image = cvLoadImage(IMAGEPATH, 0);
        //imwrite("samples/gray.jpg", new Mat(image)); // Save gray version of image*/


        /*Gaussian blur*/
        Mat filtered = new Mat();
        GaussianBlur(new Mat(image), filtered, new Size(5,5), 0);
        IplImage filteredIm = new IplImage(filtered);
        imwrite("samples/filtered.jpg", new Mat(filteredIm));

        /*Binarising Image*/
        IplImage binimg = cvCreateImage(filteredIm.asCvMat().cvSize(), IPL_DEPTH_8U, 1);
        cvThreshold(filteredIm, binimg, 0, 255, CV_THRESH_OTSU);
        //imwrite("samples/binarise.jpg", new Mat(binimg)); // Save binarised version of image*/

        /*Invert image */
        Mat inverted = new Mat();
        bitwise_not(new Mat(binimg), inverted);
        IplImage inverimg = new IplImage(inverted);
        imwrite("samples/invert.jpg", new Mat(inverimg)); // Save dilated version of image*/


//        /*Dilate image to increase the thickness of each digit*/
//        IplImage dilated = cvCreateImage(inverimg.asCvMat().cvSize(), IPL_DEPTH_8U, 1);
//        opencv_imgproc.cvDilate(inverimg, dilated, null, 1);
//        imwrite("samples/dilated.jpg", new Mat(dilated)); // Save dilated version of image*/

        /*Find countour */
        CvMemStorage storage = cvCreateMemStorage(0);
        CvSeq contours = new CvSeq();
        cvFindContours(inverimg.clone(), storage, contours, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
        CvSeq ptr = new CvSeq();
        List<Rect> rects = new ArrayList<>();
        for (ptr = contours; ptr != null; ptr = ptr.h_next()) {
            CvRect boundbox = cvBoundingRect(ptr, 1);
            Rect rect = new Rect(boundbox.x(), boundbox.y(), boundbox.width(), boundbox.height());
            rects.add(rect);
            cvRectangle(image, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    CV_RGB(0, 0, 0), 2, 0, 0);
        }

        Mat result = new Mat(image);
        Collections.sort(rects, new RectComparator());

        for (int i = 0; i < rects.size(); i++) {
            Rect rect = rects.get(i);
            Mat digit = new Mat(inverimg).apply(rect);
            copyMakeBorder(digit, digit, 10, 10, 10, 10, BORDER_CONSTANT, new Scalar(0, 0, 0, 0));
            resize(digit, digit, new Size(28, 28));
            NativeImageLoader loader = new NativeImageLoader(28, 28, 1);
            INDArray dig = loader.asMatrix(digit);
            INDArray flaten = dig.reshape(new int[]{1, 784});
            INDArray output = restored.output(flaten);
            /*for (int i = 0; i < 10; i++) {
            System.out.println("Probability of being " + i + " is " + output.getFloat(i));
            System.out.println("\n");
            }*/
            long idx = (Long) Nd4j.getExecutioner().execAndReturn(new IAMax(output)).getFinalResult();
            int idxInt = (int) idx;
            System.out.println("Best Result is : " + DIGITS[idxInt]);
            opencv_imgproc.putText(result, DIGITS[idxInt] + "", new Point(rect.x(), rect.y()), 0, 1.0, new Scalar(0, 0, 0, 0));//print result above every digit
            imwrite("samples/digit" + i + ".jpg", digit);// save digits images */
        }

        imwrite("samples/res.jpg", result);//save final result
        displayImage(result);
    }
}