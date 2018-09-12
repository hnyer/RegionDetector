package com.junmeng.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.junmeng.Iinterface.ReginListener;
import com.junmeng.bean.VectorPathInfo;
import com.junmeng.bean.MapPathInfo;
import com.junmeng.utils.CommonUtil;
import com.junmeng.utils.MoveGestureDetector;
import com.junmeng.utils.VectorMapParser;
import com.junmeng.region.R;

import java.util.HashMap;
import java.util.Map;




public class RegionDetectSurfaceView extends BaseSurfaceView {
    private static final String TAG = "RegionDetectSurface";


    /**
     * 中心定位检测模式
     */
    public static final int REGION_DETECT_MODE_CENTER = 0;
    /**
     * 手动点击检测模式
     */
    public static final int REGION_DETECT_MODE_CLICK = 1;

    @IntDef({REGION_DETECT_MODE_CENTER, REGION_DETECT_MODE_CLICK})
    /**地图检测模式*/
    public @interface RegionDetectMode {  }

    /**
     * 放大
     */
    public static final int SCALE_ZOOMIN = 0;
    /**
     * 缩小
     */
    public static final int SCALE_ZOOMOUT = 1;

    @IntDef({SCALE_ZOOMIN, SCALE_ZOOMOUT})
    public @interface ScaleMode {  }

    /**
     * 中心点图标以图标中心为中心点
     */
    public static final int CENTER_ICON_POSITION_CENTER = 0;
    /**
     * 中心点图标以底部为中心点
     */
    public static final int CENTER_ICON_POSITION_BOTTOM = 1;

    @IntDef({CENTER_ICON_POSITION_CENTER, CENTER_ICON_POSITION_BOTTOM})
    public @interface CenterIconLocationType {  }


    @CenterIconLocationType
    private int centerIconLocationType = CENTER_ICON_POSITION_BOTTOM;

    @RegionDetectMode
    private int regionDetectMode = REGION_DETECT_MODE_CENTER;

    /**从xml中解析出的vector信息*/
    private VectorPathInfo vectorPathInfo;
    private VectorMapParser vectorMapParser;
    private Paint paint = new Paint();
    private ReginListener reginListener;
    /**当前的缩放比例*/
    private float scale = 1f;
    /**最小的缩放比例*/
    private float originalScale = 1f;
    /**最大的缩放比例*/
    private float maxScale = 3.0f;

    /**平移的差值，相对于起始位置的差值*/
    private float translateDx = 0;
    private float translateDy = 0;
    /**最原始的平移差值*/
    private float originalTranslateDy = 0;
    private float originalTranslateDx = 0;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private float screenCenterX = 0;
    private float screenCenterY = 0;
    /**地图原始宽高，与xml中vector的viewportWidth和viewportHeight一致*/
    private int mapOriginalWidth = 700;
    private int mapOriginalHeight = 600;
    /**地图起始位置的中心坐标*/
    private float mapOriginalCenterX = mapOriginalWidth / 2f;
    private float mapOriginalCenterY = mapOriginalHeight / 2f;
    /**中心定位点的图标*/
    private Bitmap centerIcon;
    private MoveGestureDetector moveGestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    /**地图的矩阵，用于缩放*/
    private Matrix mapPathMatrix = new Matrix();
    /**存放地图绘制path集合*/
    private HashMap<String, MapPathInfo> pathInfoMap = new HashMap<>();
    /**当前中心定位点指向的路径path的key值*/
    private String currentKey = "";
    /**上一个中心定位点指向的路径path的key值*/
    private String lastKey = "";
    private int highlightColor = 0x80BB945A;
    private int activateAreaColor = 0x802F8BBB;
    private int normalAreaColor = 0x8069BBA8;
    private boolean isCenterIconVisible = true;
    private boolean isSupportDoubleScale = true;
    private int animateTime = 300;
    /**是否启用中心定位点*/
    private boolean isOpenCenterLocation = true;
    /**选中的激活区域（会高亮显示），只有在isOpenCenterLocation为false才生效*/
    private String selectedActivateKey = "";



    public RegionDetectSurfaceView(Context context) {
        this(context, null);
    }

    public RegionDetectSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RegionDetectSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        moveGestureDetector =  new MoveGestureDetector(context, new MyMoveGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        vectorMapParser = new VectorMapParser();
        centerIcon = CommonUtil.getBitmapFromVectorDrawable(context, R.drawable.ic_location_24dp);
        setAreaMap(R.drawable.ic_map_china);
        initPaint();
    }

    //******************************************开放接口*****************************************

    /**
     * 设置区域检测模式(支持中心定位检测和手动点击检测两种)
     * 手动点击模式符合一般使用习惯，但区域较小时很难点击
       中心定位模式则可以较细致的定位，推荐使用，默认也是此模式
     */
    public void setRegionDetectMode(@RegionDetectMode int detectMode) {
        this.regionDetectMode = detectMode;
    }

    /**
     * 设置中心图标的定位位置
     * @param locationType 图标的定位中心位置
     *  效果好像不明显
     */
    public void setCenterIconLocationType(@CenterIconLocationType int locationType) {
        this.centerIconLocationType = locationType;
    }

    /**
     * 自定义中心定位图标
     */
    public void setCenterIcon(Bitmap bitmap) {
        if (bitmap != null) {
            this.centerIcon = bitmap;
        }
    }

    /**
     * 将地图适合屏幕缩放并居中
     */
    public void fitCenter() {
        animateToFitCenter();
    }

    /**
     *设置区域高亮颜色，激活颜色和普通颜色
     此设置优先级高于默认的颜色
     * @param highlightColor 高亮颜色,-1表示不设置
     * @param activatedColor 激活颜色,-1表示不设置
     * @param normalColor    正常颜色,-1表示不设置
     */
    public void setAreaColor(@StringRes int areaNameRes, @ColorInt int highlightColor, @ColorInt int activatedColor, @ColorInt int normalColor) {
        String key = getResources().getString(areaNameRes);
        MapPathInfo info = pathInfoMap.get(key);
        if (info != null) {
            info.highlightColor = highlightColor;
            info.activatedColor = activatedColor;
            info.normalColor = normalColor;
        }
        
    }

    /**
     * 设置区域颜色
     *
     * @param highlightColor 高亮颜色,-1表示不设置
     * @param activatedColor 激活颜色,-1表示不设置
     * @param normalColor    正常颜色,-1表示不设置
     */
    public void setAreaColor(@NonNull String areaName, @ColorInt int highlightColor, @ColorInt int activatedColor, @ColorInt int normalColor) {
        MapPathInfo info = pathInfoMap.get(areaName);
        if (info != null) {
            info.highlightColor = highlightColor;
            info.activatedColor = activatedColor;
            info.normalColor = normalColor;
        }
    }

    /**
     * 设置默认的高亮颜色，优先级最低
     *
     * @param color
     */
    public void setDefaultHighlightColor(@ColorInt int color) {
        this.highlightColor = color;
        
    }

    /**
     * 设置默认的激活颜色，优先级最低
     *
     * @param color
     */
    public void setDefaultActivateColor(@ColorInt int color) {
        this.activateAreaColor = color;
        
    }

    /**
     * 设置默认的正常颜色，优先级最低
     *
     * @param color
     */
    public void setDefaultNormalColor(@ColorInt int color) {
        this.normalAreaColor = color;
        // invalidate();
    }

    /**
     * 设置区域激活状态
     *
     * @param areaNameRes
     * @param isActivated
     */
    public void setAreaActivateStatus(@StringRes int areaNameRes, boolean isActivated) {
        String key = getResources().getString(areaNameRes);
        if (pathInfoMap.get(key) != null) {

            pathInfoMap.get(key).isActivated = isActivated;
        }
        
    }

    /**
     * 设置区域激活状态
     * @param isActivated 是否激活
     */
    public void setAreaActivateStatus(@NonNull @StringRes int[] areaNameRes, boolean isActivated) {
        for (int res : areaNameRes) {
            String key = getResources().getString(res);
            if (pathInfoMap.get(key) != null) {
                pathInfoMap.get(key).isActivated = isActivated;
            }
        }

    }


    /**
     * 设置区域激活状态
     *
     * @param areaNames
     */
    public void setAreaActivateStatus(@NonNull String[] areaNames, boolean isActivated) {
        for (String key : areaNames) {
            if (pathInfoMap.get(key) != null) {
                pathInfoMap.get(key).isActivated = isActivated;
            }
        }
    }

    /**
     * 设置区域激活状态
     *
     * @param areaName
     */
    public void setAreaActivateStatus(String areaName, boolean isActivated) {
        if (pathInfoMap.get(areaName) != null) {
            pathInfoMap.get(areaName).isActivated = isActivated;
        }
    }

    /**
     * 设置是否激活所有区域
     *
     * @param isActivated
     */
    public void setAllAreaActivateStatus(boolean isActivated) {
        for (String key : pathInfoMap.keySet()) {
            pathInfoMap.get(key).isActivated = isActivated;
        }
        
    }



    /**
     * 自定义区域地图
     * @param map vector图
     */
    public void setAreaMap( int map) {
        vectorPathInfo = vectorMapParser.parse(getResources(), map);
        this.mapOriginalWidth = (int) vectorPathInfo.getViewportWidth();
        this.mapOriginalHeight = (int) vectorPathInfo.getViewportHeight();
        initAreaPathMap();
        initScaleAndTranslate();
    }


    /**
     * 设置选中的激活区域（在关闭中心定位点的情况下才生效，表现为高亮显示)
     */
    public void setSelectedAreaOnlyCloseCenterLocation(String areaName) {
        if (TextUtils.isEmpty(areaName)) {
            return;
        }
        selectedActivateKey = areaName;
    }

    /**
     * 设置选中的激活区域（在关闭中心定位点的情况下才生效，表现为高亮显示)
     *
     * @param areaNameRes
     */
    public void setSelectedAreaOnlyCloseCenterLocation(@StringRes int areaNameRes) {
        String areaName = getResources().getString(areaNameRes);
        if (TextUtils.isEmpty(areaName)) {
            return;
        }
        selectedActivateKey = areaName;
    }

    /**
     * 是否启用中心定位点，关闭则如普通地图一样只能缩放位移
     * 在中心定位检测模式下设置有效
     *
     * @param isOpen
     */
    public void isOpenCenterLocation(boolean isOpen) {
        if (regionDetectMode == REGION_DETECT_MODE_CENTER) {
            isOpenCenterLocation = isOpen;
            isCenterIconVisible = isOpen;
        }

    }

    /**
     * 检测坐标所在的区域
     *
     * @param x
     * @param y
     * @return 检测到的区域名称
     */
    public String detectRegionByCoordinate(float x, float y) {
        PointF pf = getCoordinateOpppsiteToOriginalMap(x, y);
        String areaName = "";
        //区域检测
        for (String key : pathInfoMap.keySet()) {
            Region region = pathInfoMap.get(key).region;
            if (region.contains((int) pf.x, (int) pf.y)) {
                areaName = key;
                break;
            }
        }
        return areaName;
    }

    /**
     * 设置缩放动画时间,默认300毫秒
     *
     * @param ms 毫秒
     */
    public void setAnimateTime(int ms) {
        this.animateTime = ms;
    }

    /**
     * 是否支持双击缩放操作,默认支持
     *
     * @param isSupport
     */
    public void isSupportDoubleClickScale(boolean isSupport) {
        isSupportDoubleScale = isSupport;
    }

    /**
     * 设置最大缩放比例（默认为最小的3倍），如果小于默认的最小比例则设置无效
     *
     * @param scale
     */
    public void setMaxScale(float scale) {
        if (scale > originalScale) {
            this.maxScale = scale;
        }
    }




    /**
     * 缩放地图
     *
     * @param scale
     */
    public void scaleMap(float scale) {
        mapPathMatrix.postScale(scale, scale, mapOriginalCenterX, mapOriginalCenterY);
        this.scale *= scale;
        Log.i(TAG, "onScale: " + scale);

    }

    /**
     * 平移地图
     *
     * @param translateDx
     * @param translateDy
     */
    public void translateMap(float translateDx, float translateDy) {
        this.translateDx = translateDx;
        this.translateDy = translateDy;
    }


    /**
     * 设置中心图标是否可见
     */
    public void setCenterIconVisibility(boolean isVisible) {
        isCenterIconVisible = isVisible;
    }

    /**
     * 获得所有VectorDrawableCompat中的所有path,包含第一级组里的path(暂不支持第二级及更深层次的)
     */
    public Map<String, Path> getAllPath() {
        return CommonUtil.getPaths(vectorPathInfo.getPaths());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        moveGestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = getMeasuredWidth();
        screenHeight = getMeasuredHeight();
        initScaleAndTranslate();
    }


    @Override
    public void doDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(translateDx, translateDy);

        paint.setColor(Color.GRAY);
        //复制一份，防止引发 ConcurrentModificationException
        Map<String, MapPathInfo> copyMap = new HashMap<>();
        copyMap.putAll(pathInfoMap);
        for (String key : copyMap.keySet()) {
            MapPathInfo mapPathInfo = copyMap.get(key);
            Path path = new Path();
            path.addPath(mapPathInfo.path, mapPathMatrix);

            if (currentKey.equals(key)) {
                //被选中的
                if (mapPathInfo.isActivated) {
                    //是否激活了的
                    paint.setColor(isOpenCenterLocation ?  (mapPathInfo.highlightColor != -1 ? mapPathInfo.highlightColor : highlightColor)
                            : (mapPathInfo.activatedColor != -1 ? mapPathInfo.activatedColor : activateAreaColor));
                } else {
                    paint.setColor(mapPathInfo.normalColor != -1 ? mapPathInfo.normalColor : normalAreaColor);
                }

            } else {
                if (mapPathInfo.isActivated) {
                    paint.setColor(mapPathInfo.activatedColor != -1 ? mapPathInfo.activatedColor : activateAreaColor);
                } else {
                    paint.setColor(mapPathInfo.normalColor != -1 ? mapPathInfo.normalColor : normalAreaColor);
                }
            }

            //只有在关闭中心定位点并选中了激活区域才会高亮显示
            if (!isOpenCenterLocation && mapPathInfo.isActivated && selectedActivateKey.equals(key)) {
                paint.setColor((mapPathInfo.highlightColor != -1 ? mapPathInfo.highlightColor : highlightColor));
            }
            canvas.drawPath(path, paint);

        }

        canvas.restore();

        //绘制中心定位点
        if (centerIcon != null && isCenterIconVisible && regionDetectMode == REGION_DETECT_MODE_CENTER) {
            canvas.drawBitmap(centerIcon, (screenCenterX - centerIcon.getWidth() / 2f),
                    centerIconLocationType == CENTER_ICON_POSITION_BOTTOM ?
                            (screenCenterY - centerIcon.getHeight()) :
                            (screenCenterY - centerIcon.getHeight() / 2f), null);
        }
    }


    /**
     * 初始化区域路径Map
     */
    private void initAreaPathMap() {
        pathInfoMap.clear();
        HashMap<String, Path> pathsMap = (HashMap<String, Path>) getAllPath();
        for (String key : pathsMap.keySet()) {
            MapPathInfo mapPathInfo = new MapPathInfo(pathsMap.get(key));
            pathInfoMap.put(key, mapPathInfo);
        }
    }

    private void initPaint() {
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
    }

    /**
     * 初始化原始的缩放和位移等相关变量
     */
    private void initScaleAndTranslate() {
        screenCenterX = screenWidth / 2f;
        screenCenterY = screenHeight / 2f;
        mapOriginalCenterX = mapOriginalWidth / 2f;
        mapOriginalCenterY = mapOriginalHeight / 2f;
        maxScale = originalScale * 3;
        originalTranslateDx = translateDx = (screenWidth - mapOriginalWidth) / 2f;
        originalTranslateDy = translateDy = (screenHeight - mapOriginalHeight) / 2f;
        mapPathMatrix.setScale(originalScale, originalScale, mapOriginalCenterX, mapOriginalCenterY);
    }

    /**
     * 区域检测
     */
    private void areaDetect() {
        if (!isOpenCenterLocation || regionDetectMode != REGION_DETECT_MODE_CENTER) {
            return;
        }
        currentKey = "";
        boolean isActivate = false;
        //区域检测
        for (String key : pathInfoMap.keySet()) {
            Region region = pathInfoMap.get(key).region;
            if (reginListener == null && pathInfoMap.get(key).isActivated == false) {
                continue;
            }
            if (region.contains((int) getMapCenterOppositeCoordinate().x, (int) getMapCenterOppositeCoordinate().y)) {
                Log.i(TAG, "选中了 " + key);
                currentKey = key;
                isActivate = pathInfoMap.get(key).isActivated;
                break;
            }
        }
        if (reginListener != null) {
            if (!lastKey.equals(currentKey)) {
                reginListener.onRegionDetect(currentKey);
            }

        }
        if (reginListener != null) {
            if (!lastKey.equals(currentKey)) {
                reginListener.onActivateRegionDetect(isActivate ? currentKey : "");
            }

        }
        lastKey = currentKey;
    }


    /**
     * 获得中心定位点在地图起始位置的相对坐标
     * 主要用于区域检测
     */
    private PointF getMapCenterOppositeCoordinate() {

        PointF mapCenter = getMapCenterCoordinate();
        //中心定位点与实际地图中点的差值
        float dx = screenCenterX - mapCenter.x;
        float dy = screenCenterY - mapCenter.y;

        //根据位移的差值和缩放比例计算中心定位点在地图起始位置的相对坐标
        //主要用于区域检测
        float x = mapOriginalCenterX + dx / scale;
        float y = mapOriginalCenterY + dy / scale;
        return new PointF(x, y);
    }

    /**
     * 获得指定点在起始地图的相对坐标
     * 主要用于区域检测
     * @param x 绝对坐标x
     * @param y 绝对坐标y
     */
    private PointF getCoordinateOpppsiteToOriginalMap(float x, float y) {

        PointF mapCenter = getMapCenterCoordinate();
        //中心定位点与实际地图中点的差值
        float dx = x - mapCenter.x;
        float dy = y - mapCenter.y;

        //根据位移的差值和缩放比例计算中心定位点在地图起始位置的相对坐标
        //主要用于区域检测
        float ox = mapOriginalCenterX + dx / scale;
        float oy = mapOriginalCenterY + dy / scale;
        return new PointF(ox, oy);
    }


    /**
     * 获得实际地图（即经过平移和缩放后的地图）的中心坐标
     */
    private PointF getMapCenterCoordinate() {

        float mapCenterX = mapOriginalWidth / 2f + translateDx;
        float mapCenterY = mapOriginalHeight / 2f + translateDy;

        return new PointF(mapCenterX, mapCenterY);
    }


    private class MyMoveGestureListener extends MoveGestureDetector.SimpleOnMoveGestureListener {

        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF d = detector.getFocusDelta();
            if (Math.abs(d.x) < 2 && Math.abs(d.y) < 2) {
                //忽略小范围的滑动
                return true;
            }
            translateDx += d.x;
            translateDy += d.y;
            areaDetect();
            return true;
        }

        @Override
        public boolean onMoveBegin(MoveGestureDetector detector) {
            return super.onMoveBegin(detector);
        }

        @Override
        public void onMoveEnd(MoveGestureDetector detector) {
            super.onMoveEnd(detector);

        }
    }

    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            if (Math.abs(factor - 1) < 0.015) {
                return true;
            }
            scaleMap(factor);
            areaDetect();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if (scale > maxScale) {
                animateToTargetScale(maxScale);
            }
            if (scale < originalScale) {
                animateToTargetScale(originalScale);
            }
        }
    }



    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //双击
            if (!isSupportDoubleScale) {
                return super.onDoubleTap(e);
            }
            if (scale >= maxScale) {
                //缩小为原来的大小
                if (reginListener != null) {
                    reginListener.onDoubleClick(SCALE_ZOOMOUT);
                }
                animateToFitCenter();
            } else {//缩放到最大值
                if (reginListener != null) {
                    reginListener.onDoubleClick(SCALE_ZOOMIN);
                }
                animateToTargetScale(maxScale);
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (regionDetectMode == REGION_DETECT_MODE_CLICK) {
                String name = detectRegionByCoordinate(e.getX(), e.getY());
                if (reginListener != null) {
                    reginListener.onRegionDetect(name);
                }
                MapPathInfo mapPathInfo = pathInfoMap.get(name);
                if (mapPathInfo != null && mapPathInfo.isActivated && reginListener != null) {
                    currentKey = name;
                    reginListener.onActivateRegionDetect(name);
                }
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    /**
     * 缩放到目标比例targetScale
     */
    private void animateToTargetScale(final float targetScale) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(scale, targetScale);
        valueAnimator.setDuration(animateTime);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatorValue = (float) animation.getAnimatedValue();
                scaleMap(animatorValue / scale);
                if (animatorValue == targetScale) {
                    areaDetect();
                }
            }
        });
        valueAnimator.start();
    }

    /**
     * 缩放到合适居中的比例
     */
    private void animateToFitCenter() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(scale, originalScale);
        valueAnimator.setDuration(animateTime);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatorValue = (float) animation.getAnimatedValue();

                scaleMap(animatorValue / scale);
            }
        });

        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(translateDx, originalTranslateDx);
        valueAnimator2.setDuration(animateTime);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatorValue = (float) animation.getAnimatedValue();


                translateMap(animatorValue, translateDy);

            }
        });

        ValueAnimator valueAnimator3 = ValueAnimator.ofFloat(translateDy, originalTranslateDy);
        valueAnimator3.setDuration(animateTime);
        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatorValue = (float) animation.getAnimatedValue();

                translateMap(translateDx, animatorValue);
                if (animatorValue == originalTranslateDy) {
                    areaDetect();
                }
            }
        });
        valueAnimator.start();
        valueAnimator2.start();
        valueAnimator3.start();
    }

    public void setReginListener(ReginListener reginListener) {
        this.reginListener = reginListener;
    }
}
