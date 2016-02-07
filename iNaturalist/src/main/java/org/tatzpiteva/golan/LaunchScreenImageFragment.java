package org.tatzpiteva.golan;


import android.os.Bundle;
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


    public LaunchScreenImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rv = inflater.inflate(R.layout.fragment_launch_screen_image, container, false);

        UrlImageViewHelper.setUrlDrawable(
                (ImageView) rv.findViewById(R.id.carousel_image), getArguments().getString(ARG_IMAGE));

        return rv;
    }

    public static LaunchScreenImageFragment newInsance(String imageUrl) {
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE, imageUrl);

        LaunchScreenImageFragment rv = new LaunchScreenImageFragment();
        rv.setArguments(args);

        return rv;
    }

}
