package org.tatzpiteva.golan;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

import org.inaturalist.android.INaturalistApp;
import org.inaturalist.android.INaturalistMapActivity;
import org.inaturalist.android.INaturalistMapActivityWithDefaultProject;
import org.inaturalist.android.INaturalistPrefsActivity;
import org.inaturalist.android.INaturalistService;
import org.inaturalist.android.Observation;
import org.inaturalist.android.ObservationDetails;
import org.inaturalist.android.ObservationEditor;
import org.inaturalist.android.ObservationListActivity;
import org.inaturalist.android.OnboardingActivity;
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
    private boolean exitPending;
    private ProgressDialog showObservationProgress;
    private Button buttonTatzpiteva;

    private final static int REQUEST_CODE_NEW_OBSERVATION = 0x341;

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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progress_launch_screen_loading).setVisibility(View.GONE);
                }
            });
            fillMyProjects();
        }
    };

    private final BroadcastReceiver mProjectsLoadingStartListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progress_launch_screen_loading).setVisibility(View.VISIBLE);
                }
            });
        }
    };

    private final BroadcastReceiver mObservationDetailsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);

            if (LaunchScreenActivity.this.showObservationProgress != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LaunchScreenActivity.this.showObservationProgress != null) {
                            LaunchScreenActivity.this.showObservationProgress.dismiss();
                            LaunchScreenActivity.this.showObservationProgress = null;
                        }
                    }
                });
            }

            Observation observation = (Observation) intent.getSerializableExtra(INaturalistService.OBSERVATION_RESULT);
            Intent detailsIntent = new Intent(LaunchScreenActivity.this, ObservationDetails.class)
                    .putExtra("observation", observation.toJSONObject().toString());
            startActivity(detailsIntent);
        }
    };

    @Override
    public void onBackPressed() {
        if (this.exitPending) {
            super.onBackPressed();
        }
        else {
            Toast.makeText(this, R.string.exit_confirmation_message, Toast.LENGTH_SHORT).show();
            this.exitPending = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LaunchScreenActivity.this.exitPending = false;
                }
            }, 2500);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        INaturalistApp app = (INaturalistApp) getApplication();
        if (!isLoggedIn() && !app.shownOnboarding()) {
            app.setShownOnboarding(true);
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.putExtra(OnboardingActivity.SHOW_SKIP, true);
            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        final ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        this.carouselHandler = new Handler();

        this.dotsLayout = (LinearLayout) findViewById(R.id.launch_carousel_dots);

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

        /* setup configuration manager */
        this.configManager = new LaunchScreenCarouselManager(this);
        if (this.configManager.getCachedConfig() != null) {
            this.config = this.configManager.getCachedConfig();
            ((ViewPagerAdapter) viewPager.getAdapter()).setConfig(config);
            setupDots();
        }
        this.configManager.retrieveCarouselItems(this);
        this.configManager.setOnConfigRefreshListener(new LaunchScreenCarouselManager.ConfigRefreshListener() {
            @Override
            public void onCarouselConfigRefresh(LaunchScreenCarouselConfig config) {
                if (config != null) {
                    LaunchScreenActivity.this.config = config;
                    ((ViewPagerAdapter) viewPager.getAdapter()).setConfig(config);
                    setupDots();
                }
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
                                startActivityForResult(intent, REQUEST_CODE_NEW_OBSERVATION);
                                break;
                            case R.id.upload_photo:
                                intent = new Intent(Intent.ACTION_INSERT, Observation.CONTENT_URI, LaunchScreenActivity.this, ObservationEditor.class);
                                intent.putExtra(ObservationEditor.CHOOSE_PHOTO, true);
                                startActivityForResult(intent, REQUEST_CODE_NEW_OBSERVATION);
                                break;
                            case R.id.text:
                                intent = new Intent(Intent.ACTION_INSERT, Observation.CONTENT_URI, LaunchScreenActivity.this, ObservationEditor.class);
                                startActivityForResult(intent, REQUEST_CODE_NEW_OBSERVATION);
                                break;
                        }
                    }
                }).show();
            }
        });

        findViewById(R.id.button_launch_screen_my_obs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMyObservations();
            }
        });

        findViewById(R.id.button_launch_screen_tatzpiteva).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProjectActivity(ConfigurationManager.getInstance().getConfig().getAutoUserJoinProject());
            }
        });

        findViewById(R.id.button_launch_screen_explore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startExploreActivity();
            }
        });

        /* setup settings button: underline its text and add click handler */
        findViewById(R.id.launch_screen_settings_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LaunchScreenActivity.this, INaturalistPrefsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });

        buttonTatzpiteva = (Button) findViewById(R.id.button_launch_screen_tatzpiteva);

        carouselHandler.postDelayed(carouselNextItem, CAROUSEL_AUTO_SCROLL_DELAY);
        fillMyProjects();

        registerReceiver(mProjectsChangeReceiver, new IntentFilter(MyProjectsManager.ACTION_MY_PROJECTS_LOADED));

        registerReceiver(mProjectsLoadingStartListener,
                new IntentFilter(MyProjectsManager.ACTION_MY_PROJECTS_LOADING_STARTED));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_OBSERVATION:
                launchMyObservations();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void startProjectActivity(int projectId) {
        final Intent intent = new Intent(LaunchScreenActivity.this, INaturalistMapActivityWithDefaultProject.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(INaturalistMapActivity.INTENT_PARAM_PROJECT_ID, projectId);
        startActivity(intent);
    }

    private void startExploreActivity() {
        startActivity(new Intent(LaunchScreenActivity.this, INaturalistMapActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mProjectsLoadingStartListener);
        unregisterReceiver(mProjectsChangeReceiver);
        super.onDestroy();
    }

    private void fillMyProjects() {
        Collection<MyProjectsManager.Project> projects = MyProjectsManager.getInstance().getProjects();

        LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.layout_project_buttons);

        /* if the user is logged out, show only Tatzpiteva button */
        if (!isLoggedIn()) {
            buttonsLayout.removeAllViewsInLayout();
            buttonsLayout.addView(buttonTatzpiteva);
            return;
        }

        if (projects.size() == 0) {
            findViewById(R.id.progress_launch_screen_loading).setVisibility(isLoggedIn() ? View.VISIBLE : View.GONE);
            return;
        }
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

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
        return prefs.getString("username", null) != null;
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

    private void launchMyObservations() {
        startActivity(new Intent(LaunchScreenActivity.this, ObservationListActivity.class));
    }

    //region LaunchScreenImageFragment.LaunchScreenImageTapped

    @Override
    public void onLaunchScreenImageTapped(int observationId) {
        Intent serviceIntent = new Intent(
                INaturalistService.ACTION_GET_OBSERVATION, null, this, INaturalistService.class);

        serviceIntent.putExtra(INaturalistService.OBSERVATION_ID, observationId);
        registerReceiver(mObservationDetailsReceiver, new IntentFilter(INaturalistService.ACTION_OBSERVATION_RESULT));
        startService(serviceIntent);

        this.showObservationProgress = ProgressDialog.show(this, getString(R.string.loading),
                getString(R.string.loading_observation_details));
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
