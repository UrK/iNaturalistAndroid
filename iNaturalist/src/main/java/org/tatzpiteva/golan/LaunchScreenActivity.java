package org.tatzpiteva.golan;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cocosw.bottomsheet.BottomSheet;

import org.inaturalist.android.BaseFragmentActivity;
import org.inaturalist.android.INaturalistMapActivity;
import org.inaturalist.android.INaturalistMapActivityWithDefaultProject;
import org.inaturalist.android.INaturalistService;
import org.inaturalist.android.Observation;
import org.inaturalist.android.ObservationDetails;
import org.inaturalist.android.ObservationEditor;
import org.inaturalist.android.ObservationListActivity;
import org.inaturalist.android.R;

import java.util.Collection;

public class LaunchScreenActivity extends FragmentActivity implements
        LaunchScreenImageFragment.LaunchScreenImageTapped {

    private final Integer CAROUSEL_AUTO_SCROLL_DELAY = 5000;

    private LaunchScreenCarouselManager configManager;
    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private LaunchScreenCarouselConfig config;
    private Handler carouselHandler;

    private final Runnable carouselNextItem = new Runnable() {
        @Override
        public void run() {
            if (config == null || config.getPics() == null) {
                return;
            }
            int currentItem = viewPager.getCurrentItem();
            currentItem++;
            if (currentItem >= config.getPics().size()) {
                currentItem = 0;
            }

            final int finalCurrentItem = currentItem;
            viewPager.post(new Runnable() {
                @Override
                public void run() {
                    viewPager.setCurrentItem(finalCurrentItem);
                }
            });
        }
    };

    private final BroadcastReceiver mProjectsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fillMyProjects();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        final ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        this.carouselHandler = new Handler();

        this.dotsLayout = (LinearLayout) findViewById(R.id.launch_carousel_dots);

        this.configManager = new LaunchScreenCarouselManager();
        this.configManager.retrieveCarouselItems(this);
        this.configManager.setOnConfigRefreshListener(new LaunchScreenCarouselManager.ConfigRefreshListener() {
            @Override
            public void onCarouselConfigRefresh(LaunchScreenCarouselConfig config) {
                LaunchScreenActivity.this.config = config;
                ((ViewPagerAdapter) viewPager.getAdapter()).setConfig(config);
                setupDots();
            }
        });

        this.viewPager = (ViewPager) findViewById(R.id.pager_launch_carousel);
        this.viewPager.setAdapter(pagerAdapter);
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    carouselHandler.postDelayed(carouselNextItem, CAROUSEL_AUTO_SCROLL_DELAY);
                } else {
                    carouselHandler.removeCallbacks(carouselNextItem);
                }
            }

            @Override
            public void onPageSelected(int position) {
                setupDots();
            }
        });

        findViewById(R.id.button_launch_screen_new_obs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new BottomSheet.Builder(LaunchScreenActivity.this).sheet(R.menu.observation_list_menu).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        switch (which) {
                            case R.id.camera:
                                intent = new Intent(Intent.ACTION_INSERT, Observation.CONTENT_URI, LaunchScreenActivity.this, ObservationEditor.class);
                                intent.putExtra(ObservationEditor.TAKE_PHOTO, true);
                                startActivity(intent);
                                break;
                            case R.id.upload_photo:
                                intent = new Intent(Intent.ACTION_INSERT, Observation.CONTENT_URI, LaunchScreenActivity.this, ObservationEditor.class);
                                intent.putExtra(ObservationEditor.CHOOSE_PHOTO, true);
                                startActivity(intent);
                                break;
                            case R.id.text:
                                intent = new Intent(Intent.ACTION_INSERT, Observation.CONTENT_URI, LaunchScreenActivity.this, ObservationEditor.class);
                                startActivity(intent);
                                break;
                        }
                    }
                }).show();
            }
        });

        findViewById(R.id.button_launch_screen_my_obs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LaunchScreenActivity.this, ObservationListActivity.class));
            }
        });

        findViewById(R.id.button_launch_screen_tatzpiteva).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProjectActivity(ConfigurationManager.getInstance().getConfig().getAutoUserJoinProject());
            }
        });

        carouselHandler.postDelayed(carouselNextItem, CAROUSEL_AUTO_SCROLL_DELAY);
        fillMyProjects();

        registerReceiver(mProjectsChangeReceiver, new IntentFilter(MyProjectsManager.ACTION_MY_PROJECTS_LOADED));
    }

    private void startProjectActivity(int projectId) {
        final Intent intent = new Intent(LaunchScreenActivity.this, INaturalistMapActivityWithDefaultProject.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(INaturalistMapActivity.INTENT_PARAM_PROJECT_ID, projectId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mProjectsChangeReceiver);
    }

    private void fillMyProjects() {
        Collection<MyProjectsManager.Project> projects = MyProjectsManager.getInstance().getProjects();
        if (projects.size() == 0) {
            return;
        }
        LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.layout_project_buttons);
        buttonsLayout.removeAllViewsInLayout();
        for (final MyProjectsManager.Project p : projects) {
            Button b = new Button(this);

            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(
                    0,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()),
                    0,
                    0);

            b.setLayoutParams(lp);
            b.setText(p.title);

            b.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_button_launch_project, 0, 0, 0);
            b.setBackgroundResource(R.drawable.button_launch_screen);
            b.setTextColor(getResources().getColor(android.R.color.black));
            b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            b.setCompoundDrawablePadding((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
            b.setPaddingRelative(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()),
                    0,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()),
                    0);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startProjectActivity(p.id);
                }
            });

            buttonsLayout.addView(b);
        }

    }

    private void setupDots() {
        dotsLayout.removeAllViewsInLayout();
        if (this.config == null) {
            return;
        }
        for (int i = 0; i < this.config.getPics().size(); i++) {
            ImageView iv = new ImageView(this);

            Drawable dotImage;

            int imageId = (viewPager.getCurrentItem() == i) ?
                    R.drawable.ic_launch_active_dot : R.drawable.ic_launch_inactive_dot;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dotImage = getResources().getDrawable(imageId, getTheme());
            } else {
                dotImage = getResources().getDrawable(imageId);
            }

            iv.setImageDrawable(dotImage);
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(getResources().getDimensionPixelSize(R.dimen.launch_screen_pager_margin), 0, 0, 0);
            iv.setLayoutParams(lp);
            dotsLayout.addView(iv);
        }
    }

    //region LaunchScreenImageFragment.LaunchScreenImageTapped

    @Override
    public void onLaunchScreenImageTapped(int observationId) {
//        Intent intent = new Intent(this, ObservationDetails.class);
//        intent.putExtra("observation", Integer.toString(observationId));
//        startActivity(intent);
    }

    //endregion


    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private LaunchScreenCarouselConfig config;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (this.config == null || this.config.getPics() == null) {
                return null;
            }

            final LaunchScreenCarouselConfig.Pic pic = this.config.getPics().get(position);
            return LaunchScreenImageFragment.newInsance(pic.getId(), pic.getUrl());
        }

        @Override
        public int getCount() {
            return (this.config == null || this.config.getPics() == null) ? 0 : this.config.getPics().size();
        }

        public void setConfig(LaunchScreenCarouselConfig config) {
            this.config = config;
            notifyDataSetChanged();
        }
    }
}
