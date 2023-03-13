package com.veeja.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.veeja.constant.Constant;
import com.veeja.entity.PlateFileEntity;
import com.veeja.entity.TempPlateFileEntity;
import com.veeja.enumtype.PlateColor;
import com.veeja.mapper.PlateFileMapper;
import com.veeja.mapper.TempPlateFileMapper;
import com.veeja.service.PlateService;
import com.veeja.util.FileUtil;
import com.veeja.util.GenerateIdUtil;
import com.veeja.util.PlateUtil;


/**
 * @author Veeja
 */
@Service
public class PlateServiceImpl implements PlateService {

    @Autowired
    private PlateFileMapper plateFileMapper;

    @Autowired
    private TempPlateFileMapper tempPlateFileMapper;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Object refreshFileInfo() {
        File baseDir = new File(Constant.DEFAULT_DIR);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return null;
        }
        List<TempPlateFileEntity> resultList = Lists.newArrayList();

        // 获取baseDir下第一层级的目录， 仅获取文件夹，不递归子目录，遍历
        List<File> folderList = FileUtil.listFile(baseDir, ";", false);
        folderList.parallelStream().forEach(folder -> {
            if (!folder.getName().equals("temp")) {
                // 遍历每一个文件夹， 递归获取文件夹下的图片
                List<File> imgList = FileUtil.listFile(folder, Constant.DEFAULT_TYPE, true);
                if (null != imgList && imgList.size() > 0) {
                    imgList.parallelStream().forEach(n -> {
                        TempPlateFileEntity entity = new TempPlateFileEntity();
                        entity.setFilePath(n.getAbsolutePath().replaceAll("\\\\", "/"));
                        entity.setFileName(n.getName());
                        entity.setFileType(n.getName().substring(n.getName().lastIndexOf(".") + 1));
                        resultList.add(entity);
                    });
                }
            }
        });

        tempPlateFileMapper.turncateTable();
        tempPlateFileMapper.batchInsert(resultList);
        tempPlateFileMapper.updateFileInfo();
        return 1;
    }


    /**
     * 识别方法
     *
     * @param filePath
     * @param reRecognise
     * @return
     * @author VeejaLiu
     */
    @Override
    public Object recognise(String filePath, boolean reRecognise) {
        filePath = filePath.replaceAll("\\\\", "/");
        File f = new File(filePath);
        PlateFileEntity entity = null;

        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("filePath", filePath);
        List<PlateFileEntity> list = plateFileMapper.selectByCondition(paramMap);
        if (null == list || list.size() <= 0) {
            if (FileUtil.checkFile(f)) {
                entity = new PlateFileEntity();
                entity.setFileName(f.getName());
                entity.setFilePath(f.getAbsolutePath().replaceAll("\\\\", "/"));
                entity.setFileType(f.getName().substring(f.getName().lastIndexOf(".") + 1));
                plateFileMapper.insertSelective(entity);
            }
            reRecognise = true;
        } else {
            entity = list.get(0);
        }

        if (reRecognise || StringUtils.isEmpty(entity.getTempPath())) {
            // 重新识别
            doRecognise(f, entity);
            // 重新识别之后，重新获取一下数据
            entity = plateFileMapper.selectByPrimaryKey(entity.getId());
        }
        // 查询debug文件
        if (!StringUtils.isEmpty(entity.getTempPath())) {
            Vector<String> debugFiles = new Vector<String>();
            FileUtil.getFiles(entity.getTempPath(), debugFiles);
            entity.setDebugFiles(debugFiles);
        }
        return entity;
    }

    @Override
    public Object recogniseAll() {
        // 查询到还没有进行车牌识别的图片
        List<PlateFileEntity> list = plateFileMapper.getUnRecogniseList();
        list.parallelStream().forEach(n -> {
            File f = new File(n.getFilePath());
            if (FileUtil.checkFile(f)) {
                doRecognise(f, n);
            }
        });
        return 1;
    }


    /**
     * 识别车牌号
     * @param file 车牌照片
     * @param entity 车牌文件实体
     * @return 1
     */
    public Object doRecognise(File file, PlateFileEntity entity) {
        // 如果文件不存在，则返回 null
        if (!file.exists()) {
            return null;
        }

        // 生成一个不重复的字符串 id
        String ct = GenerateIdUtil.getStrId();

        // 设置目标路径
        String targetPath = Constant.DEFAULT_TEMP_DIR + ct + (file.getName().substring(file.getName().lastIndexOf(".")));
        // 拷贝文件并且重命名
        FileUtil.copyAndRename(file.getAbsolutePath(), targetPath);

        // 创建临时目录， 存放过程图片
        String tempPath = Constant.DEFAULT_TEMP_DIR + ct + "/";
        FileUtil.createDir(tempPath);
        entity.setTempPath(tempPath);

        Boolean debug = true;
        // 存放车牌图块的 Vector
        Vector<Mat> dst = new Vector<Mat>();

        // 获取车牌号图块
        PlateUtil.getPlateMat(targetPath, dst, debug, tempPath);

        // 存放识别出的车牌号
        Set<String> plates = Sets.newHashSet();
        // 遍历车牌图块，识别车牌号
        dst.stream().forEach(inMat -> {
            // 车牌颜色
            PlateColor plateColor = PlateColor.BLUE;
            // 识别车牌号
            String plate = PlateUtil.charsSegment(inMat, plateColor, debug, tempPath);
            if (plate != null) {
                // 将识别出的车牌号添加进 Set
                plates.add("<" + plate + "," + plateColor.desc + ">");
            }
        });

        // 将识别出的车牌号存入实体类中
        entity.setRecoPlate(plates.toString());

        // 删除拷贝的临时文件
        new File(targetPath).delete();
        // 更新数据库中的实体类
        plateFileMapper.updateByPrimaryKeySelective(entity);
        return 1;
    }

    @Override
    public Object getImgInfo(String imgPath) {
        Map<String, Object> result = Maps.newHashMap();
        String ct = GenerateIdUtil.getStrId();
        File f = new File(imgPath);
        if (f.exists()) {
            String targetPath = Constant.DEFAULT_TEMP_DIR + ct + (f.getName().substring(f.getName().lastIndexOf(".")));
            FileUtil.copyAndRename(f.getAbsolutePath(), targetPath);
            // 返回临时路径给前端
            result.put("targetPath", targetPath);
            // 获取图片的基本信息
            Mat inMat = Imgcodecs.imread(targetPath);
            result.put("rows", inMat.rows());
            result.put("cols", inMat.cols());
        }
        return result;
    }


    @Override
    public Object getHSVValue(String imgPath, Integer row, Integer col) {
        Map<String, Object> result = Maps.newHashMap();
        Mat inMat = Imgcodecs.imread(imgPath);

        double[] rgb = inMat.get(row, col);
        result.put("RGB", JSONObject.toJSONString(rgb));

        Mat dst = new Mat(inMat.rows(), inMat.cols(), CvType.CV_32FC3);
        // 转到HSV空间进行处理
        Imgproc.cvtColor(inMat, dst, Imgproc.COLOR_BGR2HSV);

        double[] hsv = dst.get(row, col);
        result.put("HSV", (int) hsv[0] + ", " + (int) hsv[1] + ", " + (int) hsv[2]);
        return result;
    }


}
