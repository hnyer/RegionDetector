package com.junmeng.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.junmeng.Iinterface.ReginListener;
import com.junmeng.region.R;
import com.junmeng.utils.CommonUtil;
import com.junmeng.widget.RegionDetectSurfaceView;

import static com.junmeng.widget.RegionDetectSurfaceView.SCALE_ZOOMIN;

public class RegionDetectSViewActivity extends AppCompatActivity {

    /***
     * 激活区域
     */
    int[] areaRes = new int[]{
            R.string.china_anhui, R.string.china_beijing, R.string.china_guangdong
            , R.string.china_chongqing, R.string.china_xinjiang, R.string.china_fujian
            , R.string.china_gansu, R.string.china_zhejiang, R.string.china_yunnan
            , R.string.china_xizang, R.string.china_tianjin
            , R.string.china_shandong, R.string.china_heilongjiang, R.string.china_hainan
    };

    int[] areaRes2 = new int[]{
            R.string.china_anhui, R.string.china_beijing, R.string.china_guangdong
            , R.string.china_chongqing, R.string.china_shanxi, R.string.china_neimenggu
            , R.string.china_fujian, R.string.china_gansu, R.string.china_zhejiang
            , R.string.china_yunnan, R.string.china_liaoning, R.string.china_tianjin
            , R.string.china_shandong, R.string.china_heilongjiang, R.string.china_hainan
    };

    RegionDetectSurfaceView regionDetectSurfaceView ;
    private TextView  tvActivate;
    private TextView tvDetect ;
    private TextView  tvZoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_detect_sview);
        regionDetectSurfaceView = findViewById(R.id.regionDetectSurfaceView) ;
        tvActivate = findViewById(R.id.tvActivate) ;
        tvDetect = findViewById(R.id.tvDetect) ;
        tvZoom = findViewById(R.id.tvZoom) ;

        regionDetectSurfaceView.setReginListener(new ReginListener() {
            @Override
            public void onRegionDetect(String name) {
                tvDetect.setText("当前区域：" + name);
            }

            @Override
            public void onActivateRegionDetect(String name) {
                tvActivate.setText("高亮区域：" + name);
                regionDetectSurfaceView.setSelectedAreaOnlyCloseCenterLocation(name);
            }

            @Override
            public void onDoubleClick(int scaleMode) {
                tvZoom.setText("双击操作:" + (scaleMode == SCALE_ZOOMIN ? "放大" : "缩小"));
            }
        });




       regionDetectSurfaceView.setAreaActivateStatus(areaRes, true);
       regionDetectSurfaceView.setAreaColor(R.string.china_guangdong, Color.YELLOW, -1, -1);
       regionDetectSurfaceView.setCenterIconVisibility(true);
       regionDetectSurfaceView.setDefaultNormalColor(0x8069BBA8);
       regionDetectSurfaceView.setDefaultActivateColor(0x802F8BBB);
       regionDetectSurfaceView.setDefaultHighlightColor(0x80BB945A);
       regionDetectSurfaceView.setBackgroundColor(0xFFdddddd);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.m_fitcenter:
                // 居中
               regionDetectSurfaceView.fitCenter();
                break;
            case R.id.m_change_icon:
                // 更换图标
               regionDetectSurfaceView.setCenterIcon(CommonUtil.getBitmapFromVectorDrawable(this,R.mipmap.ic_launcher));
                break;
            case R.id.m_toggle:
                // 切换地图
               regionDetectSurfaceView.setAreaMap(R.drawable.ic_china);
               regionDetectSurfaceView.setAreaActivateStatus(areaRes2, true);
               regionDetectSurfaceView.setAreaColor(R.string.china_guangdong, Color.YELLOW, -1, -1);
               regionDetectSurfaceView.setDefaultNormalColor(0x8069BBA8);
               regionDetectSurfaceView.setDefaultActivateColor(0x802F8BBB);
               regionDetectSurfaceView.setDefaultHighlightColor(0x80BB945A);
               regionDetectSurfaceView.setBackgroundColor(0xFFFFCAF3);
                break;
            case R.id.m_mode_center:
                // 中心定位检测
               regionDetectSurfaceView.setRegionDetectMode(RegionDetectSurfaceView.REGION_DETECT_MODE_CENTER);
                break;
            case R.id.m_mode_click:
                // 手动点击检测
               regionDetectSurfaceView.setRegionDetectMode(RegionDetectSurfaceView.REGION_DETECT_MODE_CLICK);
                break;
                default:
                    break;
        }
        return super.onOptionsItemSelected(item);
    }
}
