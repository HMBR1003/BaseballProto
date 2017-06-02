package org.androidtown.baseballproto.MainFragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidtown.baseballproto.Test.DBTestActivity;
import org.androidtown.baseballproto.Test.DataTestActivity;
import org.androidtown.baseballproto.MainActivity;
import org.androidtown.baseballproto.R;
import org.androidtown.baseballproto.databinding.FragmentHomeBinding;

/**
 * Created by Administrator on 2017-05-08.
 */

public class HomeFragment extends Fragment {
    private FragmentHomeBinding homeBinding;
    ViewPager viewPager;
    MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_home,container,false);
        mainActivity = (MainActivity)getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        homeBinding = FragmentHomeBinding.bind(getView());

        homeBinding.testButtonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                Intent intent = new Intent(activity,DataTestActivity.class);
                startActivity(intent);
            }
        });
        homeBinding.testButtonHome2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                Intent intent = new Intent(activity,DBTestActivity.class);
                startActivity(intent);
            }
        });

        homeBinding.deliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) mainActivity.findViewById(R.id.vp_horizontal_ntb);
                viewPager.setCurrentItem(1);
            }
        });
        homeBinding.takeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) mainActivity.findViewById(R.id.vp_horizontal_ntb);
                viewPager.setCurrentItem(2);
            }
        });
        homeBinding.baseinfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) mainActivity.findViewById(R.id.vp_horizontal_ntb);
                viewPager.setCurrentItem(3);
            }
        });
    }
}
