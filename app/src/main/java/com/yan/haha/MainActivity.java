package com.yan.haha;

import android.animation.Animator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.yan.haha.utils.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Fragment mContentFragment = null;
    private static MainActivity instance;
    public DrawerLayout drawer;
    public ActionBarDrawerToggle toggle;
    public NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 默认打开主页面
        replaceContentFragment(new InitFragment(),false);
        navigationView.setCheckedItem(R.id.nav_home);

        // 设置开启页
        setupStartLogo();
    }

    private void setupStartLogo() {
        final ViewGroup startLogo = (ViewGroup) findViewById(R.id.start_logo);

        startLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("yan", "onTouch");
                return true;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 21) {
                    int width = Utils.getScreenWidth();
                    int height = Utils.getScreenHeight();
                    Animator anim = ViewAnimationUtils.createCircularReveal(
                            startLogo, width / 2, height / 2, height, 0);
                    anim.setDuration(500);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startLogo.setVisibility(View.GONE);
                        }

                        public void onAnimationStart(Animator animation) {}
                        public void onAnimationCancel(Animator animation) {}
                        public void onAnimationRepeat(Animator animation) {}
                    });
                    anim.start();
                } else {
                    startLogo.setVisibility(View.GONE);
                }
            }
        }, 1500);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if (getFragmentManager().getBackStackEntryCount() > 0 ) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // 主页面
            replaceContentFragment(new InitFragment(),false);
            setTitle(getString(R.string.app_name));
        } else if (id == R.id.nav_brain_riddles) {
            // 脑筋急转弯
            replaceContentFragment(new BrainRiddleFragment(),false);
            setTitle(getString(R.string.brain_riddles));
        } else if (id == R.id.nav_jokes) {
            // 冷笑话
            replaceContentFragment(new JokeFragment(),false);
            setTitle(getString(R.string.jokes));
        } else if (id == R.id.nav_horoscope) {
            // 星座
            HoroscopeFragment horoscope = new HoroscopeFragment();
            //horoscope.setSlimMode(true);
            replaceContentFragment(horoscope,false);
            setTitle(getString(R.string.horoscope));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceContentFragment(Fragment fragment, boolean addToBackStack) {
        mContentFragment = fragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragment);
        if(addToBackStack) {
            transaction.addToBackStack("fragment");
        }
        transaction.commit();
    }
}
