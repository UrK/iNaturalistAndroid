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
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

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

        this.configManager = new LaunchScreenCarouselManager();
        this.configManager.retrieveCarouselItems(this);
        this.configManager.setOnConfigRefreshListener(new LaunchScreenCarouselManager.ConfigRefreshListener() {
            @Override
            public void onCarouselConfigRefresh(LaunchScreenCarouselConfig config) {
                LinearLayout layoutPics = (LinearLayout) findViewById(R.id.layout_view_carousel);
                layoutPics.removeAllViewsInLayout();

                for (LaunchScreenCarouselConfig.Pic pic : config.getPics()) {
                    ImageView iv = new ImageView(LaunchScreenActivity.this);
                    iv.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    iv.setAdjustViewBounds(true);
                    UrlImageViewHelper.setUrlDrawable(iv, pic.getUrl());
                    layoutPics.addView(iv);

                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                }
            }
        });

        findViewById(R.id.button_new_observation).setOnClickListener(new View.OnClickListener() {
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
}
