package com.veeja.service;


public interface PlateService {
    
    public Object recognise(String filePath, boolean reRecognise);

    public Object refreshFileInfo();
    
    public Object recogniseAll();
    
    public Object getImgInfo(String imgPath);
    
    public Object getHSVValue(String imgPath, Integer row, Integer col);
    
}