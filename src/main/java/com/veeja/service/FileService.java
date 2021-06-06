package com.veeja.service;

import java.io.File;
import java.util.List;

import com.alibaba.fastjson.JSONObject;


public interface FileService {
    
    List<JSONObject> getFileTreeByDir(String rootPath, String dir, String typeFilter);
    
    File readFile(String filePath);
    
    
}