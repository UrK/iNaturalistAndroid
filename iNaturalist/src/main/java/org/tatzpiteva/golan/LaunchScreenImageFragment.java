package org.tatzpiteva.golan;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.inaturalist.android.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LaunchScreenImageFragment extends Fragment {

    private final static String ARG_IMAGE = "image";
    private final static String ARG_OBS_ID = "obsId";

    @Nullable
    private LaunchScreenImageTapped listener;

    interface LaunchScreenImageTapped {
        void onLaunchScreenImageTapped(int observationId);
    }

    public LaunchScreenImageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rv = inflater.inflate(R.layout.fragment_launch_screen_image, container, false);

        UrlImageViewHelper.setUrlDrawable(
                (ImageView) rv.findViewById(R.id.carousel_image), getArguments().getString(ARG_IMAGE));

        rv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onLaunchScreenImageTapped(getArguments().getInt(ARG_OBS_ID));
                }
            }
        });

        return rv;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof LaunchScreenImageTapped) {
            listener = (LaunchScreenImageTapped) activity;
        } else {
            throw new RuntimeException(activity.toString() + " should implement LaunchScreenImageTapped");
        }
    }

    public static LaunchScreenImageFragment newInsance(Integer observationId, String imageUrl) {
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE, imageUrl);
        args.putInt(ARG_OBS_ID, observationId);

        LaunchScreenImageFragment rv = new LaunchScreenImageFragment();
        rv.setArguments(args);

        return rv;
    }

}
