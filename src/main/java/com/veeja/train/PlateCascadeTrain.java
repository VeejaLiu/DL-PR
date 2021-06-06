package com.veeja.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.google.common.collect.Maps;
import com.veeja.constant.Constant;
import com.veeja.util.ImageUtil;

/**
 * 车牌识别训练、检测 (级联分类器)
 * 
 * 训练自己的级联分类器 -- 调用opencv的exe应用程序
 * https://blog.csdn.net/dbzzcz/article/details/105517946
 *  官方教程地址：
 *  https://docs.opencv.org/master/dc/d88/tutorial_traincascade.html
 * 
 * 获取opencv_traincascade.exe应用程序，官方下载3.4.1版本的安装包，解压出来即可找到
 * 或者加入 1054836232 Q群, 在群共享文件中获取应用程序安装包
 *
 * @author veeja
 * @date 2020-09-15 12:32
 */
public class PlateCascadeTrain {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = Constant.DEFAULT_DIR + "train/cascade_sample/";
    
    private static final String MODEL_PATH = Constant.DEFAULT_DIR + "train/cascade_sample/data";

    // 应用程序所在目录
    private static final String EXE_BASE_PATH = "D:/OpenCV/3.4.1/build/x64/vc14/bin/";
    private static final String EXE_TRAINCASCADE= EXE_BASE_PATH  + "opencv_traincascade.exe";
    private static final String EXE_CREATESAMPLES= EXE_BASE_PATH  + "opencv_createsamples.exe";

    // 
    private static Integer width = Constant.DEFAULT_WIDTH;
    private static Integer height = Constant.DEFAULT_HEIGHT;

    private static final String posInfo = "pos.info"; // 正样本文件路径数据，一行代表一个文件
    private static final String negInfo = "neg.info"; // 负样本文件路径数据，一行代表一个文件
    private static final String posVec = "pos.vec"; // 包含正样本的vec文件；  cmd_createsample 生成


    /**
     * 读取正负样本文件； 开始训练
     * 训练出来的模型文件，用于识别图片中是否包含车牌
     */
    public static void train() {
        Map<String, Object> param = Maps.newLinkedHashMap();
        param.put("data", MODEL_PATH);
        param.put("vec", DEFAULT_PATH + posVec);
        param.put("bg", DEFAULT_PATH + negInfo);
        param.put("numPos", 400);
        param.put("numNeg", 400);
        param.put("numStages", 16);
        param.put("minhitrate", 0.99);
        param.put("maxfalsealarm", 0.45);
        param.put("featureType", "LBP"); // LBP   //HAAR模型训练贼慢，400个正样本的模型，一个stage训练一个多小时
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
            for (Entry<String, Object> entry : param.entrySet()) {  // 遍历参数
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
     * @param negative
     * @param positive
     */
    public static void loadSamples(String negative, String positive) {
        // 加载负样本
        File f = new File(negative);
        File negInfoFile = new File( f.getParent().concat("/").concat(negInfo));
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
                bw.write(f.getName() + "\\" +  img.getName() + " 1 0 0 " + width + " " + height); // 使用相对路径
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Map<String, Object> param = Maps.newLinkedHashMap();
        param.put("info", DEFAULT_PATH + posInfo);
        param.put("vec", DEFAULT_PATH + posVec);
        param.put("num", f.listFiles().length);
        param.put("w", width);
        param.put("h", height);
        execCmd(EXE_CREATESAMPLES, param);
    }


    /**
     * 预测图片中是否包含车牌
     * 加载训练完成的模型文件，识别测试图片中是否包含车牌
     */
    public static void predict() {
        // 训练结果文件路径
        String trainedModel = Constant.DEFAULT_PLATE_MODEL_PATH;
        // String trainedModel = MODEL_PATH + "/cascade.xml";
        CascadeClassifier faceDetector = new CascadeClassifier(trainedModel);
        Mat inMat = Imgcodecs.imread(DEFAULT_PATH+ "test/1.jpg");
        String targetPath =  DEFAULT_PATH + "test/result.jpg";

        Boolean debug = false;
        Mat resized = ImageUtil.narrow(inMat, 600, debug, DEFAULT_PATH);
        Mat grey = new Mat();
        ImageUtil.gray(resized, grey, debug, DEFAULT_PATH);
        Mat gsMat = new Mat();
        ImageUtil.gaussianBlur(grey, gsMat, debug, DEFAULT_PATH);

        MatOfRect faceDetections = new MatOfRect(); // 识别结果存储对象 // Rect矩形类
        faceDetector.detectMultiScale(gsMat, faceDetections); // 识别车牌
        System.out.println(String.format("识别出 %s 块车牌", faceDetections.toArray().length));

        // 在识别到部位描框
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(inMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            Imgcodecs.imwrite(targetPath, inMat);
        }
        return;
    }


    public static void main(String[] args) {

        String negative = DEFAULT_PATH + "negative/";
        String positive = DEFAULT_PATH + "positive/";

        // loadSamples(negative, positive);

        // train();

        predict();
    }

}
