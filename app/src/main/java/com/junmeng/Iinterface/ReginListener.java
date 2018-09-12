package com.junmeng.Iinterface;

import com.junmeng.widget.RegionDetectSurfaceView;

/**
 * @author Aivin , on 2018/9/11.
 */

public interface ReginListener {

    /**
     * 所有区域检测
     */
    void onRegionDetect(String name);
    /**
     * 激活区域检测
     * @param name
     */
    void onActivateRegionDetect(String name);
    /**
     * 双击事件
     * @param scaleMode
     */
    void onDoubleClick(@RegionDetectSurfaceView.ScaleMode int scaleMode);
}
