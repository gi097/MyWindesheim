/**
 * Copyright (c) 2017 Giovanni Terlingen
 * <p/>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 **/
package com.giovanniterlingen.windesheim.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.giovanniterlingen.windesheim.ApplicationLoader;
import com.giovanniterlingen.windesheim.NotificationCenter;
import com.giovanniterlingen.windesheim.R;
import com.giovanniterlingen.windesheim.controllers.CalendarController;
import com.giovanniterlingen.windesheim.controllers.CookieController;
import com.giovanniterlingen.windesheim.controllers.DatabaseController;
import com.giovanniterlingen.windesheim.controllers.NotificationController;
import com.giovanniterlingen.windesheim.view.Fragments.ScheduleFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class ScheduleActivity extends AppCompatActivity
        implements NotificationCenter.NotificationCenterDelegate {

    private View view;
    private FragmentManager fragmentManager;
    private ViewPager mPager;
    private long onPauseMillis;
    private DrawerLayout mDrawerLayout;
    private int currentDayIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!DatabaseController.getInstance().hasSchedules()) {
            Intent intent = new Intent(ScheduleActivity.this, ChooseScheduleActivity.class);
            startActivity(intent);
            super.onCreate(savedInstanceState);
            finish();
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ScheduleActivity.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getInt("notifications_type", NotificationController
                .NOTIFICATION_NOT_SET) == NotificationController.NOTIFICATION_NOT_SET) {
            editor.putInt("notifications_type", NotificationController.NOTIFICATION_ALWAYS_ON);
            editor.apply();
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        view = findViewById(R.id.coordinator_layout);

        final LinearLayout layout = findViewById(R.id.pager_container);
        int height = AdSize.SMART_BANNER.getHeightInPixels(ScheduleActivity.this);
        layout.setPadding(0, 0, 0, height);

        final AdView mAdView = findViewById(R.id.bottom_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setViewPager();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView
                .setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.manage_schedules:
                                Intent intent = new Intent(ScheduleActivity.this,
                                        ManageSchedulesActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.natschool:
                                new CookieController()
                                        .checkCookieAndIntent(ScheduleActivity.this, false);
                                break;
                            case R.id.educator:
                                new CookieController()
                                        .checkCookieAndIntent(ScheduleActivity.this, true);
                                break;
                            case R.id.downloads:
                                Intent intent1 = new Intent(ScheduleActivity.this,
                                        DownloadsActivity.class);
                                startActivity(intent1);
                                break;
                            case R.id.restore_lessons:
                                Intent intent4 = new Intent(ScheduleActivity.this,
                                        HiddenLessonsActivity.class);
                                startActivity(intent4);
                                break;
                            case R.id.about:
                                Intent intent3 = new Intent(ApplicationLoader.applicationContext,
                                        AboutActivity.class);
                                startActivity(intent3);
                                break;
                            case R.id.settings:
                                Intent intent5 = new Intent(ApplicationLoader.applicationContext,
                                        SettingsActivity.class);
                                startActivity(intent5);
                                break;

                        }
                        menuItem.setChecked(false);
                        return true;
                    }
                });
    }

    private void setViewPager() {
        fragmentManager = getSupportFragmentManager();
        mPager = findViewById(R.id.pager);

        Calendar calendar = CalendarController.getInstance().getCalendar();
        int calendarDayIndex = calendar.get(GregorianCalendar.DAY_OF_WEEK);

        if (calendarDayIndex <= GregorianCalendar.FRIDAY) {
            currentDayIndex = calendar.get(GregorianCalendar.DAY_OF_WEEK) - 2;
        }

        ScreenSlidePagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(
                getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mPager);

        if (currentDayIndex >= 0) {
            mPager.setCurrentItem(currentDayIndex);
        }
    }

    @Override
    public void onPause() {
        NotificationCenter.getInstance()
                .removeObserver(this, NotificationCenter.scheduleNeedsReload);
        onPauseMillis = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    public void onResume() {
        NotificationCenter.getInstance()
                .addObserver(this, NotificationCenter.scheduleNeedsReload);
        if (!DateUtils.isToday(onPauseMillis)) {
            setViewPager();
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("notification")) {
            boolean fromNotification = intent.getExtras().getBoolean("notification");
            if (fromNotification) {
                setViewPager();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSnackbar(final String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void updateFragmentView() {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isMenuVisible()) {
                    ((ScheduleFragment) fragment).updateLayout();
                    return;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentDayIndex == -1 || mPager.getCurrentItem() == currentDayIndex) {
            super.onBackPressed();
            return;
        }
        mPager.setCurrentItem(currentDayIndex);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.scheduleNeedsReload) {
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment != null && fragment.isMenuVisible()) {
                        ((ScheduleFragment) fragment).updateAdapter();
                        return;
                    }
                }
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Calendar calendar = CalendarController.getInstance().getCalendar();
            if (calendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY) {
                DatabaseController.getInstance().clearOldScheduleData(calendar.getTime());
                calendar.add(GregorianCalendar.DATE, 2);
            } else if (calendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
                DatabaseController.getInstance().clearOldScheduleData(calendar.getTime());
                calendar.add(GregorianCalendar.DATE, 1);
            } else {
                calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
                DatabaseController.getInstance().clearOldScheduleData(calendar.getTime());
            }
            if (position <= 4) {
                calendar.add(GregorianCalendar.DATE, position);
            } else {
                calendar.add(GregorianCalendar.DATE, position + 2);
            }

            Fragment fragment = new ScheduleFragment();
            Bundle args = new Bundle();
            args.putSerializable("date", calendar.getTime());
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == currentDayIndex) {
                return getResources().getString(R.string.today);
            }
            switch (position) {
                case 0:
                case 5:
                    return getResources().getString(R.string.monday);
                case 1:
                case 6:
                    return getResources().getString(R.string.tuesday);
                case 2:
                case 7:
                    return getResources().getString(R.string.wednesday);
                case 3:
                case 8:
                    return getResources().getString(R.string.thursday);
                case 4:
                case 9:
                    return getResources().getString(R.string.friday);
                default:
                    return "";
            }
        }
    }
}