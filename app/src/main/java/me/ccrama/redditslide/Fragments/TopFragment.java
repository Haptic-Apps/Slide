package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.ccrama.redditslide.R;


/**
 * Created by ccrama on 6/2/2015.
 */
public class TopFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        return inflater.inflate(R.layout.image, container, false);
    }

}
