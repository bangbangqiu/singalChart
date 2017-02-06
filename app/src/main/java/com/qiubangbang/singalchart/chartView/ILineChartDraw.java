package com.qiubangbang.singalchart.chartView;

import android.graphics.Canvas;

/**
 * LineChart的绘制接口
 * Created by qiubangbang on 2017/1/23.
 */

public interface ILineChartDraw {

    //1 背景层
    void drawBg(Canvas canvas);//画背景
    //2 数据展示层

    //3 标尺层
    void drawXScaleLine(Canvas canvas);//画刻x轴度值

    void drawYScaleLine(Canvas canvas);//画刻y轴度值

    void drawHorizontalDottedLine(Canvas canvas);//绘制水平虚线

    void drawVerticalDottedLine(Canvas canvas);//绘制垂直虚线

    void drawInfoShow(Canvas canvas);//绘制信息提示框

    void drawAxis(Canvas canvas);//画轴线Y X

    void drawCurveLine(Canvas canvas);//画曲线

    void drawDot(Canvas canvas);//画点

    void drawDottedSelect(Canvas canvas);//绘制选中的圆点

}
