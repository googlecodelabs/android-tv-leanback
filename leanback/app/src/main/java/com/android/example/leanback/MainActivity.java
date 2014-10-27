package com.android.example.leanback;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.Toolbar;


public class MainActivity extends Activity
        implements VideoItemFragment.OnFragmentInteractionListener {

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Toolbar mToolbar;
    private ViewPager mPager;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = getTitle();
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        mAdapter = new MyAdapter(getFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                //((VideoItemFragment)mPager.getChildAt(i)).refresh();

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        SlidingTabLayout slidingTab = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        slidingTab.setViewPager(mPager);
        slidingTab.setSelectedIndicatorColors(new int[]{getResources().getColor(android.R.color.white)});


    }


    public void restoreActionBar() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    public static class MyAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 3;

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return VideoItemFragment.newInstance(Integer.toString(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Whatever";
        }


    }

}
