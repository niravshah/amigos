package com.amigos.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.amigos.fragments.CurrentJobsTabFragment;
import com.amigos.fragments.PendingJobsTabFragment;

/**
 * Created by Nirav on 15/12/2015.
 */
public class JobTabsPagerAdapter extends FragmentStatePagerAdapter {

    public JobTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CurrentJobsTabFragment.newInstance(position);
            case 1:
                return PendingJobsTabFragment.newInstance(position);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return CurrentJobsTabFragment.TAB_NAME;
            case 1:
                return PendingJobsTabFragment.TAB_NAME;
            default:
                return null;
        }
    }
}
