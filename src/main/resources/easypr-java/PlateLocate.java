package com.veeja.easypr.core;

import java.util.Vector;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import com.veeja.constant.Constant;
import org.bytedeco.javacpp.opencv_imgcodecs;

import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.RotatedRect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;


/**
 * 车牌定位
 * @author veeja
 * @date 2020-04-24 15:33
 */
public class PlateLocate {

    // PlateLocate所用常量
    public static final int DEFAULT_GAUSSIANBLUR_SIZE = 5;
    public static final int SOBEL_SCALE = 1;
    public static final int SOBEL_DELTA = 0;
    public static final int SOBEL_DDEPTH = CV_16S;
    public static final int SOBEL_X_WEIGHT = 1;
    public static final int SOBEL_Y_WEIGHT = 0;
    public static final int DEFAULT_MORPH_SIZE_WIDTH = 17;
    public static final int DEFAULT_MORPH_SIZE_HEIGHT = 3;

    // showResultMat所用常量
    public static final int WIDTH = 136;
    public static final int HEIGHT = 36;
    public static final int TYPE = CV_8UC3;

    // verifySize所用常量
    public static final int DEFAULT_VERIFY_MIN = 3;
    public static final int DEFAULT_VERIFY_MAX = 20;

    final float DEFAULT_ERROR = 0.6f;
    final float DEFAULT_ASPECT = 3.75f;

    // 角度判断所用常量
    public static final int DEFAULT_ANGLE = 30;

    // 高斯模糊所用变量
    protected int gaussianBlurSize = DEFAULT_GAUSSIANBLUR_SIZE;

    // 连接操作所用变量
    protected int morphSizeWidth = DEFAULT_MORPH_SIZE_WIDTH;
    protected int morphSizeHeight = DEFAULT_MORPH_SIZE_HEIGHT;

    // verifySize所用变量
    protected float error = DEFAULT_ERROR;
    protected float aspect = DEFAULT_ASPECT;
    protected int verifyMin = DEFAULT_VERIFY_MIN;
    protected int verifyMax = DEFAULT_VERIFY_MAX;

    // 角度判断所用变量
    protected int angle = DEFAULT_ANGLE;

    // 是否开启调试模式，0关闭，非0开启 
    protected boolean debug = true;

    // 开启调试模式之后，切图文件保存路径
    protected String tempPath = Constant.DEFAULT_TEMP_DIR + System.currentTimeMillis() + "/";

    /**
     * 生活模式与工业模式切换
     * @param islifemode
     * 如果为真，则设置各项参数为定位生活场景照片（如百度图片）的参数，否则恢复默认值。
     * 
     */
    public void setLifemode(boolean islifemode) {
        if (islifemode) {
            setGaussianBlurSize(5);
            setMorphSizeWidth(9);
            setMorphSizeHeight(3);
            setVerifyError(0.9f);
            setVerifyAspect(4);
            setVerifyMin(1);
            setVerifyMax(30);
        } else {
            setGaussianBlurSize(DEFAULT_GAUSSIANBLUR_SIZE);
            setMorphSizeWidth(DEFAULT_MORPH_SIZE_WIDTH);
            setMorphSizeHeight(DEFAULT_MORPH_SIZE_HEIGHT);
            setVerifyError(DEFAULT_ERROR);
            setVerifyAspect(DEFAULT_ASPECT);
            setVerifyMin(DEFAULT_VERIFY_MIN);
            setVerifyMax(DEFAULT_VERIFY_MAX);
        }
    }

    /**
     * 定位车牌图像
     * @param src 原始图像
     * @return 一个Mat的向量，存储所有抓取到的图像
     */
    public Vector<Mat> plateLocate(Mat src) {
        Vector<Mat> resultVec = new Vector<Mat>();

        Mat src_blur = new Mat();
        Mat src_gray = new Mat();
        Mat grad = new Mat();

        int scale = SOBEL_SCALE;
        int delta = SOBEL_DELTA;
        int ddepth = SOBEL_DDEPTH;

        // 高斯模糊。Size中的数字影响车牌定位的效果。
        GaussianBlur(src, src_blur, new Size(gaussianBlurSize, gaussianBlurSize), 0, 0, BORDER_DEFAULT);
        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_GaussianBlur.jpg", src_blur);
        }

        // Convert it to gray 将图像进行灰度化
        cvtColor(src_blur, src_gray, CV_RGB2GRAY);
        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_gray.jpg", src_gray);
        }

        // 对图像进行Sobel 运算，得到的是图像的一阶水平方向导数。

        // Generate grad_x and grad_y
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_x, abs_grad_x);

        Sobel(src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_y, abs_grad_y);

        // Total Gradient (approximate)
        addWeighted(abs_grad_x, SOBEL_X_WEIGHT, abs_grad_y, SOBEL_Y_WEIGHT, 0, grad);

        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_Sobel.jpg", grad);
        }

        // 对图像进行二值化。将灰度图像（每个像素点有256 个取值可能）转化为二值图像（每个像素点仅有1 和0 两个取值可能）。

        Mat img_threshold = new Mat();
        threshold(grad, img_threshold, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);

        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_threshold.jpg", img_threshold);
        }

        // 使用闭操作。对图像进行闭操作以后，可以看到车牌区域被连接成一个矩形装的区域。

        Mat element = getStructuringElement(MORPH_RECT, new Size(morphSizeWidth, morphSizeHeight));
        morphologyEx(img_threshold, img_threshold, MORPH_CLOSE, element);

        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_morphology.jpg", img_threshold);
        }

        // Find 轮廓 of possibles plates 求轮廓。求出图中所有的轮廓。这个算法会把全图的轮廓都计算出来，因此要进行筛选。

        MatVector contours = new MatVector();
        findContours(img_threshold, contours, // a vector of contours
                CV_RETR_EXTERNAL, // 提取外部轮廓
                CV_CHAIN_APPROX_NONE); // all pixels of each contours

        Mat result = new Mat();
        if (debug) {
            src.copyTo(result);
            // 将轮廓描绘到图上输出
            drawContours(result, contours, -1, new Scalar(0, 0, 255, 255));
            opencv_imgcodecs.imwrite(tempPath + "debug_Contours.jpg", result);
        }

        // Start to iterate to each contour founded
        // 筛选。对轮廓求最小外接矩形，然后验证，不满足条件的淘汰。
        Vector<RotatedRect> rects = new Vector<RotatedRect>();

        for (int i = 0; i < contours.size(); ++i) {
            RotatedRect mr = minAreaRect(contours.get(i));
            if (verifySizes(mr))
                rects.add(mr);
        }

        int k = 1;
        for (int i = 0; i < rects.size(); i++) {
            RotatedRect minRect = rects.get(i);
            /*if (debug) {
                Point2f rect_points = new Point2f(4);
                minRect.points(rect_points);

                for (int j = 0; j < 4; j++) {
                    Point pt1 = new Point(new CvPoint2D32f(rect_points.position(j)));
                    Point pt2 = new Point(new CvPoint2D32f(rect_points.position((j + 1) % 4)));

                    line(result, pt1, pt2, new Scalar(0, 255, 255, 255), 1, 8, 0);
                }
            }*/

            // rotated rectangle drawing
            // 旋转这部分代码确实可以将某些倾斜的车牌调整正，但是它也会误将更多正的车牌搞成倾斜！所以综合考虑，还是不使用这段代码。
            // 2014-08-14,由于新到的一批图片中发现有很多车牌是倾斜的，因此决定再次尝试这段代码。

            float r = minRect.size().width() / minRect.size().height();
            float angle = minRect.angle();
            Size rect_size = new Size((int) minRect.size().width(), (int) minRect.size().height());
            if (r < 1) {
                angle = 90 + angle;
                rect_size = new Size(rect_size.height(), rect_size.width());
            }
            // 如果抓取的方块旋转超过m_angle角度，则不是车牌，放弃处理
            if (angle - this.angle < 0 && angle + this.angle > 0) {
                Mat img_rotated = new Mat();
                Mat rotmat = getRotationMatrix2D(minRect.center(), angle, 1);
                warpAffine(src, img_rotated, rotmat, src.size()); // CV_INTER_CUBIC

                Mat resultMat = showResultMat(img_rotated, rect_size, minRect.center(), k++);
                resultVec.add(resultMat);
            }
        }

        return resultVec;
    }


    /**
     * 对minAreaRect获得的最小外接矩形，用纵横比进行判断
     * 
     * @param mr
     * @return
     */
    private boolean verifySizes(RotatedRect mr) {
        float error = this.error;

        // China car plate size: 440mm*140mm，aspect 3.142857      
        float aspect = this.aspect;
        int min = 44 * 14 * verifyMin; // minimum area
        int max = 44 * 14 * verifyMax; // maximum area

        // Get only patchs that match to a respect ratio.
        float rmin = aspect - aspect * error;
        float rmax = aspect + aspect * error;

        int area = (int) (mr.size().height() * mr.size().width());
        float r = mr.size().width() / mr.size().height();
        if (r < 1)
            r = mr.size().height() / mr.size().width();

        return area >= min && area <= max && r >= rmin && r <= rmax;
    }

    /**
     * 显示最终生成的车牌图像，便于判断是否成功进行了旋转。
     * @param src
     * @param rect_size
     * @param center
     * @param index
     * @return
     */
    private Mat showResultMat(Mat src, Size rect_size, Point2f center, int index) {
        Mat img_crop = new Mat();
        getRectSubPix(src, rect_size, center, img_crop);

        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_crop_" + index + ".jpg", img_crop);
        }

        Mat resultResized = new Mat();
        resultResized.create(HEIGHT, WIDTH, TYPE);
        resize(img_crop, resultResized, resultResized.size(), 0, 0, INTER_CUBIC);
        if (debug) {
            opencv_imgcodecs.imwrite(tempPath + "debug_resize_" + index + ".jpg", resultResized);
        }
        return resultResized;
    }



    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public void setGaussianBlurSize(int gaussianBlurSize) {
        this.gaussianBlurSize = gaussianBlurSize;
    }

    public final int getGaussianBlurSize() {
        return this.gaussianBlurSize;
    }

    public void setMorphSizeWidth(int morphSizeWidth) {
        this.morphSizeWidth = morphSizeWidth;
    }

    public final int getMorphSizeWidth() {
        return this.morphSizeWidth;
    }

    public void setMorphSizeHeight(int morphSizeHeight) {
        this.morphSizeHeight = morphSizeHeight;
    }

    public final int getMorphSizeHeight() {
        return this.morphSizeHeight;
    }

    public void setVerifyError(float error) {
        this.error = error;
    }

    public final float getVerifyError() {
        return this.error;
    }

    public void setVerifyAspect(float aspect) {
        this.aspect = aspect;
    }

    public final float getVerifyAspect() {
        return this.aspect;
    }

    public void setVerifyMin(int verifyMin) {
        this.verifyMin = verifyMin;
    }

    public void setVerifyMax(int verifyMax) {
        this.verifyMax = verifyMax;
    }

    public void setJudgeAngle(int angle) {
        this.angle = angle;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public boolean getDebug() {
        return debug;
    }

}
