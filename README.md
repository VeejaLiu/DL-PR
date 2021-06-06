# DL+PR

#### 写在前面的话
- 这是一个基于spring boot + maven + opencv 实现的**Demo教程项目**
- **贯穿样本处理、模型训练、图像处理、对象检测、对象识别等技术点**

#### 包含功能
- **车牌检测及车牌号码识别**
- **网上常见的轮廓提取车牌算法JAVA实现**
- **hsv色彩分割提取车牌算法JAVA实现**
- **harrcascade特征识别算法 JAVA实现**
- **基于svm算法的车牌检测训练JAVA实现**
- **基于ann算法的车牌号码识别训练JAVA实现**

#### 相关文档
- **[00_相关问题解答.md](./doc/00_相关问题解答.md)**
主要关于识别失败、识别错误、如何优化算法的相关问题解答
- **[01_开发环境搭建.md](./doc/01_开发环境搭建.md)**
- [02_SVM训练说明文档.md -待补齐](./doc/02_SVM训练说明文档.md)
- [03_ANN训练说明文档.md -待补齐](./doc/03_ANN训练说明文档.md)
- **[04_车牌识别过程说明文档.md](./doc/04_车牌识别过程说明文档.md)**
- **[05_部分图像处理算法说明文档.md](./doc/05_车牌识别部分算法说明文档.md)**


#### 操作界面
![20200921132116.png](./doc/pic/20200921132116.png)

![20200921132208.png](./doc/pic/20200921132208.png)

![20200921132312.png](./doc/pic/20200921132312.png)

![20200921132357.png](./doc/pic/20200921132357.png)

![20200921133022.png](./doc/pic/20200921133022.png)

![20200921133221.png](./doc/pic/20200921133221.png)

![20200921133214.png](./doc/pic/20200921133214.png)

#### 软件版本
- jdk 1.8.61+
- maven 3.0+
- opencv 4.0.1 [或者：javacpp1.4.4、opencv-platform 4.0.1-1.4.4]
- tess4j 4.3.1
- spring boot 2.1.5.RELEASE


#### 参考文档
- EasyPR C++项目、以及fan-wenjie的EasyPR-Java项目；同时查阅了部分opencv官方4.0.1版本C++的源码，结合个人对java语言的理解，整理出当前项目
- liuruoze/EasyPR：https://gitee.com/easypr/EasyPR?_from=gitee_search
- fan-wenjie/EasyPR-Java： https://github.com/fan-wenjie/EasyPR-Java
- opencv官方： https://opencv.org/

