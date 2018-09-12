package com.junmeng.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;

import com.junmeng.bean.PathInfo;
import com.junmeng.bean.VectorPathInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommonUtil {

    /**
     * 根据路径生成Region
     */
    public static Region genRegion(Path path) {
        if(path==null){
            return null;
        }
        Region region = new Region();
        RectF r = new RectF();
        //得到Path的矩形边界
        path.computeBounds(r, true);
        // 设置区域路径和剪辑描述的区域
        region.setPath(path, new Region((int) (r.left), (int) (r.top),  (int) (r.right),  (int) (r.bottom)));
        return region;
    }




    /**
     * 根据vector资源id获得Bitmap
     * 需要在build.gradle中配置
     * defaultConfig {
     * vectorDrawables.useSupportLibrary = true
     * }
     *
     * @param context
     * @param vectorDrawableId
     * @return
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, int vectorDrawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, vectorDrawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 获得路径Map
     */
    public static Map<String,Path> getPaths(List<PathInfo> paths){
        Map<String,Path> map=new HashMap<>();
       if(paths==null){
           return map;
       }
       for(PathInfo info: paths){
           Path path= PathParser.createPathFromPathData(info.getPathData());
           if(path==null){
               path=new Path();
           }
           map.put(info.getName(),path);
       }
       return map;
    }
}
