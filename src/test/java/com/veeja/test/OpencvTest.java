package com.veeja.test;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.veeja.util.ImageUtil;

public class OpencvTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static String testImg = "D:/PlateDetect/test/13.png";

    private static String tempPath = "D:\\PlateDetect\\temp\\";


    /**
     * 仿射变换测试
     * @param args
     */
    public static void main(String[] args) {
        Mat inMat = Imgcodecs.imread(testImg);
        Mat dst = new Mat();

        // translateImg(inMat, dst, 100, 200);  // 右下平移
        // translateImg(inMat, dst, -100, -200); // 左上平移

        // zoom(inMat, dst, 0.5, 0.5);

        /*Point center = new Point(inMat.width() /2, inMat.height() / 2);
        double angle = -30.0;
        rotateImg(inMat, dst, angle, center, true, tempPath);*/

        // warpPerspective(inMat, dst, true, tempPath);
        
        
        
    }


    public static void warpPerspective(Mat inMat, Mat dst, Boolean debug, String tempPath){
        // 原图四个顶点
        MatOfPoint2f srcPoints = new MatOfPoint2f();
        srcPoints.fromArray(new Point(0, 0), new Point(0, inMat.rows()), new Point(inMat.cols(), 0), new Point(inMat.cols(), inMat.rows()));
        // 目标图四个顶点
        MatOfPoint2f dstPoints = new MatOfPoint2f();
        dstPoints.fromArray(new Point(0 + 80, 0), new Point(0 - 80, inMat.rows()), new Point(inMat.cols() + 80, 0) , new Point(inMat.cols() - 80, inMat.rows()));

        Mat trans_mat  = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Imgproc.warpPerspective(inMat, dst, trans_mat, inMat.size());
        ImageUtil.debugImg(debug, tempPath, "warpPerspective", dst);
    }    


    public static void rotateImg(Mat inMat, Mat dst, double angle, Point center, Boolean debug, String tempPath){
        Mat img_rotated = Imgproc.getRotationMatrix2D(center, angle, 1); // 获取旋转矩阵
        Imgproc.warpAffine(inMat, dst, img_rotated, inMat.size());
        ImageUtil.debugImg(debug, tempPath, "rotateImg", dst);
    }


    /**
     * 放大、缩小
     * @param inMat
     * @param dst
     * @param x 水平方向变换比例
     * @param y 垂直方向变换比例
     */
    public static void zoom(Mat inMat, Mat dst, Double x, Double y){
        Mat trans_mat = Mat.zeros(2, 3, CvType.CV_32FC1);
        trans_mat.put(0, 0, x);
        trans_mat.put(1, 1, y);
        Imgproc.warpAffine(inMat, dst, trans_mat, inMat.size()); // 仿射变换
        ImageUtil.debugImg(true, tempPath, "zoom", dst);
    }




    /**
     * 平移
     * @param pX 水平方向移动像素； 大于0则表示沿着轴正向移动，若小于0则表示沿着轴负向移动
     * @param pY 垂直方向移动像素； 大于0则表示沿着轴正向移动，若小于0则表示沿着轴负向移动
     */
    public static void translateImg(Mat inMat, Mat dst, int pX, int pY){
        Mat trans_mat = Mat.zeros(2, 3, CvType.CV_32FC1);    //定义平移矩阵 创建2行3列的全0矩阵
        trans_mat.put(0, 0, 1);
        trans_mat.put(0, 2, pX);
        trans_mat.put(1, 1, 1);
        trans_mat.put(1, 2, pY);
        Imgproc.warpAffine(inMat, dst, trans_mat, inMat.size()); // 仿射变换
        ImageUtil.debugImg(true, tempPath, "translate", dst);
    }

}
