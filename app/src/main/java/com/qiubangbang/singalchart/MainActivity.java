package com.qiubangbang.singalchart;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.qiubangbang.singalchart.tablayout.ChartPagerAdapter;
import com.qiubangbang.singalchart.tablayout.SlidingTabLayout;
import com.qiubangbang.singalchart.tablayout.listener.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {


    private ViewPager viewPager;
    private SlidingTabLayout tabLayout;
    private String[] titles = new String[]{"折线图", "饼形图", "柱状图", "雷达图"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        eventbind();
    }


    private void initData() {

        ChartPagerAdapter chartPagerAdapter = new ChartPagerAdapter(this);
        viewPager.setAdapter(chartPagerAdapter);
        tabLayout.setViewPager(viewPager, titles);
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (SlidingTabLayout) findViewById(R.id.slidingTab);
    }

    private void eventbind() {
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }
}
