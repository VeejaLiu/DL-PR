package com.veeja.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.google.common.collect.Maps;
import com.veeja.constant.Constant;
import com.veeja.util.FileUtil;
import com.veeja.util.GenerateIdUtil;
import com.veeja.util.ImageUtil;

/**
 * 人脸识别训练、检测 (级联分类器)
 * <p>
 * 训练自己的级联分类器 -- 调用opencv的exe应用程序
 * https://blog.csdn.net/dbzzcz/article/details/105517946
 * 官方教程地址：
 * https://docs.opencv.org/master/dc/d88/tutorial_traincascade.html
 * <p>
 * 获取opencv_traincascade.exe应用程序，官方下载3.4.1版本的安装包，解压出来即可找到
 * 或者加入 1054836232 Q群, 在群共享文件中获取应用程序安装包
 *
 * @author veeja
 * @date 2020-09-15 12:32
 */
public class CascadeTrain {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = "D:/FaceDetect/";

    // 应用程序所在目录
    private static final String EXE_BASE_PATH = "D:/OpenCV/3.4.1/build/x64/vc14/bin/";
    private static final String EXE_TRAINCASCADE = EXE_BASE_PATH + "opencv_traincascade.exe";
    private static final String EXE_CREATESAMPLES = EXE_BASE_PATH + "opencv_createsamples.exe";

    private static Integer width = 24;
    private static Integer height = 24;

    private static final String posInfo = "pos.info"; // 正样本文件路径数据，一行代表一个文件
    private static final String negInfo = "neg.info"; // 负样本文件路径数据，一行代表一个文件

    private static final String posVec = "pos.vec"; // 包含正样本的vec文件；  cmd_createsample 生成


    /**
     * 读取正负样本文件； 开始训练
     * 训练出来的模型文件，用于识别图片中是否包含人脸
     */
    public static void train() {
        Map<String, Object> param = Maps.newLinkedHashMap();
        param.put("data", DEFAULT_PATH + "samples/data/");
        param.put("vec", DEFAULT_PATH + "samples/" + posVec);
        param.put("bg", DEFAULT_PATH + "samples/" + negInfo);
        param.put("numPos", 16);
        param.put("numNeg", 16);
        param.put("numStages", 16);
        param.put("minhitrate", 0.99);
        param.put("maxfalsealarm", 0.45);
        param.put("featureType", "HAAR"); // LBP
        param.put("mode", "ALL");
        param.put("w", width);
        param.put("h", height);
        param.put("precalcValBufSize", 2048);
        param.put("precalcIdxBufSize", 2048);
        // 执行较慢  eclipse下执行无法看到执行效果； 资源管理器下查看opencv_traincascade.exe进程
        // 建议复制命令出来，在cmd窗口执行
        execCmd(EXE_TRAINCASCADE, param);
        // traincascade's error (Required leaf false alarm rate achieved. Branch training terminated.)
        // 先测试一下生成的cascade.xml，如果效果没有达到你的预期，
        // 解决方案： 1：maxfalsealarm值应该设定到0.4 - 0.5之间  2：正负样本数太少，增大样本数
    }

    public static void execCmd(String cmd, Map<String, Object> param) {
        try {
            // 遍历参数
            for (Entry<String, Object> entry : param.entrySet()) {
                cmd = cmd + " -" + entry.getKey() + " " + entry.getValue();
            }
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }


    /**
     * 加载样本文件
     *
     * @param negative
     * @param positive
     */
    public static void loadSamples(String negative, String positive) {
        // 加载负样本
        File f = new File(negative);
        File negInfoFile = new File(f.getParent().concat("/").concat(negInfo));
        FileWriter fw;
        try {
            fw = new FileWriter(negInfoFile);
            BufferedWriter bw = new BufferedWriter(fw);
            for (File img : f.listFiles()) {
                bw.write(img.getPath()); // 使用 绝对 路径
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // 加载正样本
        f = new File(positive);
        File posInfoFile = new File(f.getParent().concat("/").concat(posInfo));
        try {
            fw = new FileWriter(posInfoFile);
            BufferedWriter bw = new BufferedWriter(fw);
            for (File img : f.listFiles()) {
                bw.write(f.getName() + "\\" + img.getName() + " 1 0 0 " + width + " " + height); // 使用相对路径
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Map<String, Object> param = Maps.newLinkedHashMap();
        param.put("info", DEFAULT_PATH + "samples/" + posInfo);
        param.put("vec", DEFAULT_PATH + "samples/" + posVec);
        param.put("num", f.listFiles().length);
        param.put("w", width);
        param.put("h", height);
        execCmd(EXE_CREATESAMPLES, param);
    }


    /**
     * 预测图片中是否包含人脸
     * 加载训练完成的模型文件，识别测试图片中是否包含人脸
     */
    public static void predict() {
        // 训练结果文件路径
        String trainedModel = DEFAULT_PATH + "samples/data/cascade.xml";
        CascadeClassifier faceDetector = new CascadeClassifier(trainedModel);
        Mat inMat = Imgcodecs.imread(DEFAULT_PATH + "train/huge/huge.png");
        String targetPath = DEFAULT_PATH + "samples/result.jpg";

        Boolean debug = false;
        Mat grey = new Mat();
        ImageUtil.gray(inMat, grey, debug, DEFAULT_PATH);
        Mat gsBlur = new Mat();
        ImageUtil.gaussianBlur(grey, gsBlur, debug, DEFAULT_PATH);

        MatOfRect faceDetections = new MatOfRect(); // 识别结果存储对象 // Rect矩形类
        faceDetector.detectMultiScale(gsBlur, faceDetections); // 识别人脸
        System.out.println(String.format("识别出 %s 张人脸", faceDetections.toArray().length));

        // 在识别到部位描框
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(inMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            Imgcodecs.imwrite(targetPath, inMat);
        }
        return;
    }


    /**
     * 从图片中，提取人脸图块(正样本)
     * 将图块处理为相同大小正样本文件，放到样本训练目录下
     *
     * @param sourcePath 原图目录
     * @param targetPath 样本存放目录
     * @param limit      提取样本数量
     */
    public static void preparePosSamples(String sourcePath, String targetPath, Integer limit) {
        Vector<String> files = new Vector<String>();
        FileUtil.getFiles(sourcePath, files);
        CascadeClassifier faceDetector = new CascadeClassifier(Constant.DEFAULT_FACE_MODEL_PATH);
        int i = 0;
        for (String img : files) {
            Mat inMat = Imgcodecs.imread(img);
            if (inMat.empty()) {
                continue;
            }
            Mat gray = new Mat();
            ImageUtil.gray(inMat, gray, false, "");
            // Mat gsMat = ImageUtil.gaussianBlur(gray, false, "");
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(gray, faceDetections);
            for (Rect rect : faceDetections.toArray()) {
                // 截取人脸  灰度图
                Mat face = new Mat();
                Size size = new Size(rect.width, rect.height);
                Point center = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
                Imgproc.getRectSubPix(gray, size, center, face);

                // resize 24*24
                Mat resized = new Mat(width, height, CvType.CV_8UC3);
                Imgproc.resize(face, resized, resized.size(), 0, 0, Imgproc.INTER_CUBIC);
                // 保存文件
                Imgcodecs.imwrite(targetPath + GenerateIdUtil.getId() + ".jpg", resized);
                i++;
                if (i >= limit) {
                    return;
                }
            }
        }
        return;
    }


    /**
     * 从图片中，随机提取负样本； 放到样本训练目录下
     *
     * @param sourcePath 原图目录
     * @param targetPath 样本存放目录
     * @param limit      提取样本数量
     */
    public static void prepareNegSamples(String sourcePath, String targetPath, Integer limit) {
        Vector<String> files = new Vector<String>();
        FileUtil.getFiles(sourcePath, files);
        int i = 0;
        Random rd = new Random();
        for (String img : files) {
            Mat inMat = Imgcodecs.imread(img);
            if (inMat.empty()) {
                continue;
            }
            Mat gray = new Mat();
            ImageUtil.gray(inMat, gray, false, "");
            Mat gsMat = new Mat();
            ImageUtil.gaussianBlur(gray, gsMat, false, "");
            // 随机截取指定大小的图片，用作负样本
            Mat negMat = new Mat();
            Size size = new Size(width, height);
            Point center = new Point(rd.nextInt(inMat.cols() - 101) + 100, rd.nextInt(inMat.rows() - 101) + 100);
            Imgproc.getRectSubPix(gsMat, size, center, negMat);
            // 保存文件
            Imgcodecs.imwrite(targetPath + GenerateIdUtil.getId() + ".jpg", negMat);
            i++;
            if (i >= limit) {
                return;
            }
        }
        return;
    }


    public static void main(String[] args) {

        String sourcePath = DEFAULT_PATH + "samples\\asia_all\\";
        String targetPath = DEFAULT_PATH + "samples\\positive\\";
        // preparePosSamples(sourcePath, targetPath, 4000);

        sourcePath = "D:\\PlateDetect\\plate\\";
        targetPath = "D:\\FaceDetect\\samples\\negative\\";
        int i = 0;
        do {
            // prepareNegSamples(sourcePath, targetPath, 400);
            i++;
        } while (i <= 100);


        String negative = DEFAULT_PATH + "samples\\negative\\";
        String positive = DEFAULT_PATH + "samples\\positive\\";
        // loadSamples(negative, positive);

        // train();

        predict();

        return;
    }

}
