package org.tatzpiteva.golan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cocosw.bottomsheet.BottomSheet;

import org.inaturalist.android.Observation;
import org.inaturalist.android.ObservationEditor;
import org.inaturalist.android.R;

public class LaunchScreenActivity extends FragmentActivity {

    private LaunchScreenCarouselManager configManager;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        final ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        this.configManager = new LaunchScreenCarouselManager();
        this.configManager.retrieveCarouselItems(this);
        this.configManager.setOnConfigRefreshListener(new LaunchScreenCarouselManager.ConfigRefreshListener() {
            @Override
            public void onCarouselConfigRefresh(LaunchScreenCarouselConfig config) {
                ((ViewPagerAdapter) viewPager.getAdapter()).setConfig(config);

//                LinearLayout layoutPics = (LinearLayout) findViewById(R.id.layout_view_carousel);
//                layoutPics.removeAllViewsInLayout();
//
//                for (LaunchScreenCarouselConfig.Pic pic : config.getPics()) {
//                    ImageView iv = new ImageView(LaunchScreenActivity.this);
//                    iv.setLayoutParams(new LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
//                    iv.setAdjustViewBounds(true);
//                    layoutPics.addView(iv);
//                }
            }
        });

        this.viewPager = (ViewPager) findViewById(R.id.pager_launch_carousel);
        this.viewPager.setAdapter(pagerAdapter);

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
    }
    
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

            return LaunchScreenImageFragment.newInsance(this.config.getPics().get(position).getUrl());
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
