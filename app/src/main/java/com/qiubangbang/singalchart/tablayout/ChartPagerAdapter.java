package com.qiubangbang.singalchart.tablayout;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiubangbang.singalchart.R;
import com.qiubangbang.singalchart.chartView.LineChartView;
import com.qiubangbang.singalchart.chartView.PieChatView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by qiubangbang on 2017/1/20.
 */

public class ChartPagerAdapter extends PagerAdapter {

    /**
     * onsucess
     * 1：获取到bean ==》list
     * 2：判断list 和 allList是否都为空，是 显示无数据
     * 3：判断是否加载更多 是 清空集合
     * 4：判断是否是第一页 否显示无更多数据
     */
    Context mContext;
    int[] layout = new int[]{R.layout.adapter1
            , R.layout.adapter2, R.layout.adapter3, R.layout.adapter4};

    public ChartPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(layout[position], null);

        switch (position) {
            case 0:
                LineChartView lcv = (LineChartView) view.findViewById(R.id.lineChartView);
                lcv.SetInfo(new String[]{"(3)[3月]", "(4)[4月]", "(5)[5月]", "(6)[6月]", "(7)[7月]", "(8)[8月]"},
                        new String[]{"(22)[]", "(11)[]", "(76)[]", "(40)[]", "(110)[]", "(90)[]"});
                lcv.startAnim();
                break;
            case 1:
                PieChatView pcv = (PieChatView) view.findViewById(R.id.pcv);
                ArrayList<Integer> colors = new ArrayList<>();
                colors.add(Color.rgb(0xff, 0x64, 0x68));
                colors.add(Color.rgb(0xff, 0x8e, 0x20));
                colors.add(Color.rgb(0x5c, 0x9a, 0xe2));
                LinkedHashMap kindsMap = new LinkedHashMap<>();
                kindsMap.put("a", 23);
                kindsMap.put("b", 99);
                kindsMap.put("c", 12);
                pcv.setDataMap(kindsMap);
                pcv.setColors(colors);
                pcv.setCenterTitle("");
                pcv.setMinAngle(50);
                pcv.setmRadius(70 * 3);//// TODO: 2017/2/6  
                pcv.startDraw();

                break;
            case 2:
                break;
        }


        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
