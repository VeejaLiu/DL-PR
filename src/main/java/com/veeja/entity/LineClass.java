//package com.veeja.entity;
//
//import java.util.List;
//
//import org.opencv.core.Point;
//
//import com.google.common.collect.Lists;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//
///**
// * 线段分类实体类
// * 用来保存一组线段，相对x轴的斜率、相对原点的距离 在指定范围内的线段
// * @author veeja
// * @date 2020-12-07 13:37
// */
//@Data
//@NoArgsConstructor
//public class LineClass {
//
//    private Double distance;
//
//    private Double k;
//
//    private List<Line> lines;
//
//
//    public LineClass(Line line) {
//        setDistance(line.getDistanceToOrigin());
//        setK(line.getK());
//        lines = Lists.newArrayList();
//        lines.add(line);
//    }
//
//
//    public boolean addLine(Line line) {
//        boolean bl = true;
//        if(Math.abs(distance - line.getDistanceToOrigin()) > 5) { // 判断距离是否满足条件
//            bl = false;
//        }
//        if(Math.abs(k - line.getK()) > 5) { // 判断斜率是否满足条件
//            bl = false;
//        }
//        if(bl) {
//            lines.add(line);
//        }
//        return bl;
//    }
//
//    public Line getNewLine() {
//        if(null == lines || lines.size() <=0) {
//            return null;
//        } else {
//            Point min = null;
//            Point max = null;
//            double minSum = 1000000000000d;
//            double maxSum = 0d;
//            for (Line line : lines) {
//                double sum = line.getStart().x + line.getStart().y;
//                if(sum < minSum) {
//                    minSum = sum;
//                    min = line.getStart();
//                }
//                if(sum > maxSum) {
//                    maxSum = sum;
//                    max = line.getStart();
//                }
//                sum = line.getEnd().x + line.getEnd().y;
//                if(sum < minSum) {
//                    minSum = sum;
//                    min = line.getEnd();
//                }
//                if(sum > maxSum) {
//                    maxSum = sum;
//                    max = line.getEnd();
//                }
//            }
//            return new Line(min, max);
//        }
//    }
//
//
//}
