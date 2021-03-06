package com.qhn.bhne.xhmusic.mvp.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by qhn
 * on 2016/11/2 0002.
 */

public class OnLinePagerAdapter extends FragmentPagerAdapter {
    private List<String> mTitles;
    private List<Fragment> mFragmentList;
    private List<Integer> imageResIdList;//gpuv
    public OnLinePagerAdapter(FragmentManager fm, List<String> mTitles, List<Fragment> mFragmentList, List<Integer> imageResIdList) {
        super(fm);
        this.mTitles = mTitles;
        this.mFragmentList = mFragmentList;
        this.imageResIdList = imageResIdList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

}