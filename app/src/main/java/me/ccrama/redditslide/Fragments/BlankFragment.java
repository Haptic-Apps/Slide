package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import me.ccrama.redditslide.R;

public class BlankFragment extends Fragment {
    public View v2;
    public View realBack;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.blank_fragment, container, false);
        v2 = v.findViewById(R.id.back);
        realBack = v;
        return v;
    }

    public void doOffset(float percent) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v2.getLayoutParams();
        params.setMargins(0, 0, (int) (-v2.getWidth()*((1f -percent) * 1.25)), 0);
        v2.setLayoutParams(params);
    }
}