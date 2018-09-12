package com.junmeng.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 矢量图映射类，与xml结构对应
 */

public class VectorPathInfo {

    private double viewportHeight;
    private double viewportWidth;
    private List<PathInfo> paths=new ArrayList<>();

    public double getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(double viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public double getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(double viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public List<PathInfo> getPaths() {
        return paths;
    }

    public void setPaths(List<PathInfo> paths) {
        this.paths = paths;
    }



}
