package com.veeja.train;

import java.util.Date;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.Ml;
import org.opencv.ml.TrainData;

import com.google.common.collect.Sets;
import com.veeja.constant.Constant;
import com.veeja.util.FileUtil;
import com.veeja.util.PlateUtil;


/**
 * 基于org.opencv包实现的训练
 * 图片文字识别训练
 * 训练出来的库文件，用于识别图片中的数字及字母
 *
 * @author veeja
 */
public class ANNTrain {

    private ANN_MLP ann = ANN_MLP.create();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 默认的训练操作的根目录
     */
    private static final String DEFAULT_PATH = "D:/PlateDetect/train/chars_sample/";

    /**
     * 训练模型文件保存位置
     */
    private static final String MODEL_PATH = DEFAULT_PATH + "ann.xml";


    /**
     * 样本文件包含：
     * 1、从停车场图片提取的【蓝牌】字符样本，包含数字、字母、汉字； 19226个样本文件；可用于识别蓝牌、黄牌字符等
     * 2、从停车场图片提取的【绿牌】字符样本，包含数字、字母、汉字；    3998个样本文件；可用于识别绿牌字符； // 绿牌的部分字符跟蓝牌字体不一样，导致模型要添加蓝牌、绿牌样本
     * 3、旧项目上整理过的【蓝牌】字符样本，包含数字、字母、汉字；             16013个样本文件；可用于识别蓝牌、黄牌字符等
     * 4、旋转、错切、膨胀、腐蚀处理的【蓝牌、绿牌】样本，包含数字、字母、汉字；     101400个样本文件；可用于识别蓝牌、黄牌、绿字符等
     *
     * @param _predictsize
     * @param _neurons
     */
    public void train(int _predictsize, int _neurons) {
        // 使用push_back，行数列数不能赋初始值
        Mat samples = new Mat();
        Vector<Integer> trainingLabels = new Vector<Integer>();

        Set<String> sampleDir = Sets.newHashSet();

        // 加载数字及字母字符
        for (int i = 0; i < Constant.numCharacter; i++) {
            sampleDir.clear();
            sampleDir.add(DEFAULT_PATH + "chars_blue_new/" + Constant.strCharacters[i]);

            Vector<String> files = new Vector<String>();
            for (String str : sampleDir) {
                FileUtil.getFiles(str, files);
            }
            for (String filename : files) {
                Mat img = Imgcodecs.imread(filename, 0);
                //
                Mat f = PlateUtil.features(img, _predictsize);
                samples.push_back(f);
                trainingLabels.add(i); // 每一幅字符图片所对应的字符类别索引下标
            }
        }


        samples.convertTo(samples, CvType.CV_32F);

        //440   vhist.length + hhist.length + lowData.cols() * lowData.rows();
        // CV_32FC1 CV_32SC1 CV_32F
        Mat classes = Mat.zeros(trainingLabels.size(), Constant.strCharacters.length, CvType.CV_32F);

        float[] labels = new float[trainingLabels.size()];
        for (int i = 0; i < labels.length; ++i) {
            classes.put(i, trainingLabels.get(i), 1.f);
        }

        // samples.type() == CV_32F || samples.type() == CV_32S 
        TrainData train_data = TrainData.create(samples, Ml.ROW_SAMPLE, classes);

        ann.clear();
        Mat layers = new Mat(1, 3, CvType.CV_32F);
        // 样本特征数 140  10*10 + 20+20
        layers.put(0, 0, samples.cols());
        // 神经元个数
        layers.put(0, 1, _neurons);
        // 字符数
        layers.put(0, 2, classes.cols());

        ann.setLayerSizes(layers);
        ann.setActivationFunction(ANN_MLP.SIGMOID_SYM, 1, 1);
        ann.setTrainMethod(ANN_MLP.BACKPROP);
        TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER, 300000, 0.0001);
        ann.setTermCriteria(criteria);
        ann.setBackpropWeightScale(0.1);
        ann.setBackpropMomentumScale(0.1);
        ann.train(train_data);

        // FileStorage fsto = new FileStorage(MODEL_PATH, FileStorage.WRITE);
        // ann.write(fsto, "ann");
        ann.save(MODEL_PATH);
    }


    public void predict() {
        ann.clear();
        ann = ANN_MLP.load(MODEL_PATH);

        int total = 0;
        int correct = 0;

        Set<String> sampleDir = Sets.newHashSet();
        // 遍历测试样本下的所有文件，计算预测准确率
        for (int i = 0; i < Constant.strCharacters.length; i++) {
            char c = Constant.strCharacters[i];
            sampleDir.clear();
            sampleDir.add(DEFAULT_PATH + "chars_blue_new/" + c);
            Vector<String> files = new Vector<String>();
            for (String str : sampleDir) {
                FileUtil.getFiles(str, files);
            }
            // 遍历每一张图片
            for (String filePath : files) {
                Mat img = Imgcodecs.imread(filePath, 0);
                Mat f = PlateUtil.features(img, Constant.predictSize);
                int index = 0;
                double maxVal = -2;
                Mat output = new Mat(1, Constant.strCharacters.length, CvType.CV_32F);
                // 预测结果
                ann.predict(f, output);
                for (int j = 0; j < Constant.strCharacters.length; j++) {
                    double val = output.get(0, j)[0];
                    if (val > maxVal) {
                        maxVal = val;
                        index = j;
                    }
                }
                // 膨胀
                /*
                f = PlateUtil.features(ImageUtil.dilate(img, false, null, 2, 2, false), Constant.predictSize);
                ann.predict(f, output);  // 预测结果
                for (int j = 0; j < Constant.strCharacters.length; j++) {
                    double val = output.get(0, j)[0];
                    if (val > maxVal) {
                        maxVal = val;
                        index = j;
                    }
                }
                */

                String result = String.valueOf(Constant.strCharacters[index]);
                if (result.equals(String.valueOf(c))) {
                    correct++;
                } else {
                    /*
                    for (int j = 0; j < Constant.strCharacters.length; j++) {
                        double val = output.get(0, j)[0];
                        if(val > 0.1) {
                            System.out.println( j + "===>" + val);
                        }
                    }
                    */
                    System.err.print("原样本：" + String.valueOf(c));
                    System.err.print("\t预测结果：" + result);
                    System.err.println("\t" + filePath);
                }
                total++;
            }
        }

        System.out.print("total:" + total);
        System.out.print("\tcorrect:" + correct);
        System.out.print("\terror:" + (total - correct));
        System.out.println("\t计算准确率为：" + correct / (total * 1.0));

        return;
    }

    public static void main(String[] args) {
        Date startDate = new Date();
        ANNTrain annT = new ANNTrain();
        // 这里只训练model文件夹下的ann.xml，此模型是一个predictSize=10,neurons=40的ANN模型
        // 可根据需要训练不同的predictSize或者neurons的ANN模型
        // 根据机器的不同，训练时间不一样，但一般需要10分钟左右，所以慢慢等一会吧

        annT.train(Constant.predictSize, Constant.neurons);
        // annT.predict();
        Date endDate = new Date();
        int useTime = endDate.compareTo(startDate);
        System.out.println("训练ANN用时：" + useTime);
        return;
    }


}