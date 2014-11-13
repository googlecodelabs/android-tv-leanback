/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.leanback;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;


public class MainActivity extends Activity
        implements VideoItemFragment.OnFragmentInteractionListener {

    private ViewPager mPager;
    private MoviePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup ViewPager
        mAdapter = new MoviePagerAdapter(getFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Sliding tabs for viewpager
        SlidingTabLayout slidingTab = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        slidingTab.setViewPager(mPager);
        // slidingTab.setSelectedIndicatorColors(new int[]{getResources().getColor(android.R.color.white)});
        slidingTab.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.accent);
            }

            @Override
            public int getDividerColor(int position) {
                return Color.argb(0,0,0,0);
            }
        });


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
    public void onFragmentInteraction(String id, String url) {

    }


    /**
     *  Simple implementation for {@link #FragmentPagerAdapter}
     */
    public static class MoviePagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 3;

        public MoviePagerAdapter(FragmentManager fm) {
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
            switch(position) {
                case 0: return "Featured";
                case 1: return "Popular";
                case 2: return "Editor's Choice";
                default: return "This can't happen";

            }
        }

    }

}
