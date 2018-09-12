package com.junmeng.utils;

import android.content.res.Resources;

import com.junmeng.bean.PathInfo;
import com.junmeng.bean.VectorPathInfo;

import org.xmlpull.v1.XmlPullParser;

/**
 * vector矢量图解析器
 */

public class VectorMapParser {

    private  final String NAMESPACE = "http://schemas.android.com/apk/res/android" ;

    public VectorPathInfo parse( Resources res,   int resId) {

        VectorPathInfo vectorPathInfo = new VectorPathInfo();
        try {

            XmlPullParser parser = res.getXml(resId);
            int event = parser.getEventType();
            String tag;
            while (event != XmlPullParser.END_DOCUMENT) {
                tag = parser.getName();
                switch (event) {

                    case XmlPullParser.START_TAG:
                        if ("vector".equals(tag)) {
                            vectorPathInfo.setViewportWidth(Double.parseDouble(parser.getAttributeValue(NAMESPACE, "viewportWidth")));
                            vectorPathInfo.setViewportHeight(Double.parseDouble(parser.getAttributeValue(NAMESPACE, "viewportHeight")));
                        } else if ("path".equals(tag)) {
                            PathInfo pathInfo = new PathInfo();
                            pathInfo.setName(res.getString(Integer.parseInt(parser.getAttributeValue(NAMESPACE, "name").replace("@", ""))));
                            pathInfo.setPathData(parser.getAttributeValue(NAMESPACE, "pathData"));
                            vectorPathInfo.getPaths().add(pathInfo);
                        }
                        break;
                    default:
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vectorPathInfo;
    }


}
