package com.qiubangbang.singalchart.chartView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.qiubangbang.singalchart.R;

import java.util.ArrayList;

/**
 * 线形图
 * Created by qiubangbang on 2016/1/6.
 * copy LineChartView 数据源基本一致，数据层的绘制有区别
 */

public class BarChartView extends View {

    private Context context;

    //new Attr

    private int dotLineColor;// 虚线的颜色
    private int axisColor;// 轴的颜色
    private int scaleColor;// 字体的颜色
    private int curveLineColor;//曲线的颜色
    private int defaultColor = 0xff81d8ff;

    private Paint linePaint;
    private Paint textPaint;
    private Paint fillPaint;

    //按照原点进行相对布局
    private int dotPaddingBottom;
    private int dotPaddingLeft;
    private int axisWidth;
    private float axisXLength;
    private float axisYLength;
    private int frameWidth;
    private int frameHeight;
    private int framePaddingBottom = dipToPx(12);//计算的时候按照矩形底边的中心
    private int frameTextSize;
    private int xTextPaddingTop;
    private int yTextPaddingRight;
    private int xLeftExtend;
    private int xRightExtend;
    private int yBottomExtend;
    private int yTopExtend;
    private IMaxValueRules iMaxValueRules;
    private int internal = 4;
    int pillerWidth = dipToPx(10);//柱子宽度/2

    //数据规则 "[]"之间括文字"()"之间括数字
    String[] xLabels;
    String[] yLabels;

    //计算字段
    private int selectDot = 1;//注：从下标5才开始计算点 记得处理下,当前第几个点，从0开始
    //0是原点，1 2 x轴起始点 3 4 y轴的起始点 5 以后是y轴的点和x轴对应的各个点
    ArrayList<Point> points = new ArrayList<>();
    private int touchDownX = 0;// 触摸时刚刚点击点的横坐标值
    private int touchTempX = 0;// 手指滑动过程中横坐标值
    private int touchTotalMoveX = 0;// 最后手指离开屏幕与开始点击时的横坐标差值
    private int xlength;
    private int ylength;
    private float max;
    private float down_x = 0;
    private float down_y = 0;
    private float increasement = 1f;

    private static final String TAG = "lineChartView";

    public BarChartView(Context context) {
        super(context);
    }

    public BarChartView(Context ct, AttributeSet attrs) {
        super(ct, attrs);
        init(ct, attrs);
    }

    public BarChartView(Context ct, AttributeSet attrs, int defStyle) {
        super(ct, attrs, defStyle);
        init(ct, attrs);
    }

    private void init(Context ct, AttributeSet attrs) {
        this.context = ct;
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.DottedLine);
        dotLineColor = attributes.getColor(R.styleable.DottedLine_dotLineColor, defaultColor);
        axisColor = attributes.getColor(R.styleable.DottedLine_axisColor, defaultColor);
        scaleColor = attributes.getColor(R.styleable.DottedLine_scaleColor, defaultColor);
        curveLineColor = attributes.getColor(R.styleable.DottedLine_curveLineColor, defaultColor);

        axisWidth = (int) attributes.getDimension(R.styleable.DottedLine_axisWidth, dipToPx(2f));
        axisXLength = attributes.getFloat(R.styleable.DottedLine_axisXLength, 0.85f);
        axisYLength = attributes.getFloat(R.styleable.DottedLine_axisYLength, 0.7f);
        frameWidth = (int) attributes.getDimension(R.styleable.DottedLine_frameWidth, dipToPx(35));
        frameHeight = (int) attributes.getDimension(R.styleable.DottedLine_frameHeight, dipToPx(13));
        frameTextSize = (int) attributes.getDimension(R.styleable.DottedLine_frameTextSize, dipToPx(10));
        dotPaddingBottom = (int) attributes.getDimension(R.styleable.DottedLine_dotPaddingBottom, dipToPx(25));
        dotPaddingLeft = (int) attributes.getDimension(R.styleable.DottedLine_dotPaddingLeft, dipToPx(30));
        xTextPaddingTop = (int) attributes.getDimension(R.styleable.DottedLine_xTextPaddingTop, dipToPx(16));
        yTextPaddingRight = (int) attributes.getDimension(R.styleable.DottedLine_yTextPaddingRight, dipToPx(10));
        xLeftExtend = (int) attributes.getDimension(R.styleable.DottedLine_xLeftExtend, dipToPx(6));
        xRightExtend = (int) attributes.getDimension(R.styleable.DottedLine_xRightExtend, dipToPx(6) + pillerWidth);//这里加了个柱子宽度
        yBottomExtend = (int) attributes.getDimension(R.styleable.DottedLine_yBottomExtend, dipToPx(6));
        yTopExtend = (int) attributes.getDimension(R.styleable.DottedLine_yTopExtend, dipToPx(6));

        attributes.recycle();

        //初始化 画笔,分为三种 1 线性画笔 2 填充画笔 3 文字画笔
        //线性画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        //文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(dipToPx(10));

        //填充区域画笔
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        //初始化最大值规则
        iMaxValueRules = new IMaxValueRules() {
            @Override
            public int getMaxValue(int min, int max) {
                //保证max被4，和10 整除
                while (max % 4 != 0 | max % 10 != 0) {
                    max++;
                }
                return max;
            }
        };

    }

    /**
     * @param XLabels x轴左边的各个点
     * @param YLabels 各个点所对应的y轴的值
     */
    public void SetInfo(String[] XLabels, String[] YLabels) {
        this.xLabels = XLabels;
        this.yLabels = YLabels;
        initData();
    }

    private void initData() {
        points.clear();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("qiubit_r", "over?");
                int viewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                int viewHeight = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();
                dotPaddingBottom -= getPaddingBottom();
                dotPaddingLeft -= getPaddingLeft();
                Log.d("qiubit_r", viewWidth + ": " + viewHeight);
                xlength = (int) (axisXLength * viewWidth);
                ylength = (int) (axisYLength * viewHeight);
                //计算原点位置
                Point dotPoint = new Point(dotPaddingLeft, viewHeight - dotPaddingBottom);
                points.add(dotPoint);
                //计算x轴起始点(原点的位置为起点)
                points.add(new Point(dotPoint.x - xLeftExtend, dotPoint.y));
                points.add(new Point(dotPoint.x + xRightExtend + xlength, dotPoint.y));
                //计算y轴起始点
                points.add(new Point(dotPoint.x, dotPoint.y + yBottomExtend));
                points.add(new Point(dotPoint.x, dotPoint.y - yTopExtend - ylength));
                //todo 最大值规则临时给的
                max = iMaxValueRules.getMaxValue(0, 110);
                //计算y轴列表的坐标
                for (int i = 0; i < (internal + 1); i++) {
                    float y = dotPoint.y - ylength * i * (1.0f / internal);
                    points.add(new Point(dotPoint.x, (int) y));
                }
                //计算刻度列表的坐标
                for (int i = 0; i < xLabels.length; i++) {
                    int x = dotPoint.x + i * (xlength / (xLabels.length - 1)) + pillerWidth;
                    int y = dotPoint.y - (int) (ylength * (getDesVal(yLabels[i]) / max));
                    Log.d("qiubit_r", i + ": " + y);
                    points.add(new Point(x, y));
                }
                Log.d("qiubit_r", points.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (xLabels == null || xLabels.length == 0 || yLabels == null || yLabels.length == 0) {
            return;
        }

        //1 背景层
        drawBg(canvas);//画背景
        drawDotedDottedLine(canvas);//绘制水平和垂直虚线
//        //2 数据展示层
        drawPiller(canvas);//画柱子                +  动画
        if (increasement == 1) drawInfoShow(canvas);//绘制信息提示框
//        //3 标尺层
        drawAxis(canvas);//画轴线Y X
        drawXYScaleLine(canvas);//画刻x y轴度值
    }

    private void drawXYScaleLine(Canvas canvas) {
        textPaint.setColor(scaleColor);
        textPaint.setTextSize(dipToPx(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
        //x轴刻度值
        for (int i = 5 + internal + 1; i < points.size(); i++) {
            canvas.drawText(getDesStr(xLabels[i - 5 - internal - 1]), points.get(i).x, points.get(0).y + xTextPaddingTop, textPaint);
        }
        //y刻度值
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i < 5; i++) {//todo 固定值 把纵坐标分为4份
            canvas.drawText((int) (i * (max / 4)) + "", points.get(0).x - yTextPaddingRight, points.get(i + 5).y + dipToPx(3), textPaint);//todo 固定值 diptopx(3)
        }
    }

    private void drawAxis(Canvas canvas) {
        linePaint.setColor(axisColor);
        linePaint.setTextSize(dipToPx(10));
        linePaint.setStrokeWidth(axisWidth);
        canvas.drawLine(points.get(1).x, points.get(1).y,
                points.get(2).x, points.get(2).y, linePaint);

        canvas.drawLine(points.get(3).x, points.get(3).y,
                points.get(4).x, points.get(4).y, linePaint);
    }

    private void drawBg(Canvas canvas) {
        fillPaint.setShader(new LinearGradient(points.get(0).x, points.get(4 + internal + 1).y,
                points.get(0).x, points.get(0).y, 0xfff7f8fb, 0xffedf2f6, Shader.TileMode.CLAMP));//渐变色
        canvas.drawRect(new RectF(points.get(0).x, points.get(4 + internal + 1).y,
                points.get(4 + internal + 1 + xLabels.length).x, points.get(0).y), fillPaint);
        fillPaint.setShader(null);
    }

    private void drawDotedDottedLine(Canvas canvas) {
        linePaint.setColor(dotLineColor);
        linePaint.setStrokeWidth(dipToPx(1));
        PathEffect effects = new DashPathEffect(new float[]{dipToPx(2), dipToPx(2)}, 1);
        linePaint.setPathEffect(effects);
        //垂直虚线
        for (int i = 5 + internal + 1 + 1; i < points.size(); i++) {
            Path path = new Path();
            path.moveTo(points.get(i).x, points.get(0).y);
            path.lineTo(points.get(i).x, points.get(0).y - ylength);
            canvas.drawPath(path, linePaint);
        }
        //水平虚线
        for (int i = 6; i < 5 + internal + 1; i++) {
            Path path = new Path();
            path.moveTo(points.get(i).x, points.get(i).y);
            path.lineTo(points.get(i).x + xlength, points.get(i).y);
            canvas.drawPath(path, linePaint);
        }
        linePaint.setPathEffect(null);
    }

    // 绘制信息提示框
    private void drawInfoShow(Canvas canvas) {

        fillPaint.setColor(0xff50bbff);//todo 固定值
        Point p = points.get(5 + internal + 1 + selectDot);
        RectF oval3 = new RectF(p.x - frameWidth / 2, p.y - frameHeight - framePaddingBottom,
                p.x + frameWidth / 2, p.y - framePaddingBottom);// 设置个新的长方形
        canvas.drawRoundRect(oval3, 15, 15, fillPaint);// 第二个参数是x半径，第三个参数是y半径

        Path path2 = new Path();
        path2.moveTo(p.x, p.y - framePaddingBottom + dipToPx(3));//todo 暂时用固定值
        path2.lineTo(p.x - dipToPx(2), p.y - framePaddingBottom);
        path2.lineTo(p.x + dipToPx(2), p.y - framePaddingBottom);//todo 固定值
        canvas.drawPath(path2, fillPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(frameTextSize);
        canvas.drawText(getDesVal(yLabels[selectDot]) + "",
                p.x, p.y - framePaddingBottom - (frameHeight - frameTextSize), textPaint);//todo 这里要重新梳理计算
        textPaint.setTextAlign(Paint.Align.RIGHT);
    }

    // 绘制曲线
    private void drawPiller(Canvas canvas) {
        linePaint.setColor(curveLineColor);
        //遍历各个点
        //internal+1 是因为分为4份，其实占了5个点
        RectF rectF;
        fillPaint.setColor(defaultColor);
        for (int i = 5 + internal + 1; i < points.size(); i++) {

            int y = (int) (points.get(0).y - (points.get(0).y - points.get(i).y) * increasement);
            int x = points.get(i).x;

            if ((i - 5 - internal - 1) == selectDot) {
                fillPaint.setAlpha(250);
            } else {
                fillPaint.setAlpha(100);
            }

            //如果是最后一个月，柱子往前挪一挪
            if (i == points.size() - 1) {
                rectF = new RectF(x - pillerWidth-dipToPx(8), y, x + pillerWidth-dipToPx(8), points.get(0).y);//todo 这里用固定值
            } else {
                rectF = new RectF(x - pillerWidth, y, x + pillerWidth, points.get(0).y);
            }
            canvas.drawRect(rectF, fillPaint);
        }
        fillPaint.setAlpha(250);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = touchTempX = (int) event.getRawX();
                down_x = event.getX();
                down_y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                touchTempX = (int) event.getRawX();
                touchTotalMoveX = touchTempX - touchDownX;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(touchTotalMoveX) >= dipToPx(10)) {
                    if (touchTotalMoveX > 0) {
                        if (selectDot < (xLabels.length - 1))
                            selectDot++;
                    } else if (touchTotalMoveX < 0) {
                        if (selectDot > 0)
                            selectDot--;
                    }
                    invalidate();
                }
                touchTotalMoveX = 0;
                float up_x = event.getX();
                float up_y = event.getY();
                if (Math.abs(down_x - up_x) <= dipToPx(10)
                        && Math.abs(down_y - up_y) <= dipToPx(10)) {
                    changePointPosition(up_x, up_y);
                }
                break;
        }

        return true;
    }

    /**
     * 图标动画
     */
    public void startAnim() {
        final ValueAnimator animator = ValueAnimator.ofFloat(1);
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                increasement = f;
                invalidate();
                Log.d(TAG, "onAnimationUpdate: " + f);
                if (f == 1) {
                    animator.removeUpdateListener(this);
                    Log.d(TAG, "onAnimationUpdate: over");
                }
            }
        });
        animator.start();
    }

    /**
     * 改变提示信息的点的坐标位置
     */
    private void changePointPosition(float x, float y) {
        //为了方便点击，加大点击区域
        for (int i = 5 + internal + 1; i < points.size(); i++) {
            if (x < (points.get(i).x + pillerWidth) && x > (points.get(i).x - pillerWidth)
                    && y < points.get(0).y && y > points.get(i).y) {
                selectDot = (i - 5 - 1 - internal);
                postInvalidate();
                return;
            }
        }
    }

    private int dipToPx(float dip) {
        /**
         * 突然想整理下关于长度单位的几个概念：
         *   dpi == 像素密度 == 一(英)寸（2.54cm）包含的像素数
         *   density == 英文译为密度 == 在这里指与160dpi的比值
         *   d(i independent)p == android中长度单位 == px/density
         *
         * 感想：还是觉得百分比布局更好用，简单些 适配性也更强。推荐以后改用百分比适配
         * 不过dp比百分比好的一点是dp更精确一点，比如一条线宽就1dp 3px左右，但是换算百分比呢？1/420dp ~0.00238..?
         */
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    private String getDesStr(String str) {
        return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
    }

    private float getDesVal(String str) {
        return Float.parseFloat(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
    }

    interface IMaxValueRules {
        int getMaxValue(int min, int max);
    }
}
