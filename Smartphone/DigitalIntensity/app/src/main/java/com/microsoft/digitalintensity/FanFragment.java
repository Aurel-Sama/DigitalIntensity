package com.microsoft.digitalintensity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;


public class FanFragment extends Fragment {
    private TextView fanTv;
    private RandomThread rt;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fanTv = (TextView) view.findViewById(R.id.fan_tv);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.aldrich);
        fanTv.setTypeface(typeface);
        rt = new RandomThread(view,fanTv);
        rt.setDaemon(true);
        rt.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        rt.setValue(false);
        super.onStop();
    }

    public void setText(Random rand){
        int maxSpeed = 2000;
        int minSpeed = 400;
        int fanSpeed = rand.nextInt(maxSpeed + 1 - minSpeed) + minSpeed;
        if(fanSpeed > maxSpeed*0.75){
            fanTv.setTextColor(Color.RED);
        }
        fanTv.setText("Vitesse de rotation du ventilateur : "+fanSpeed+" rpm");

    }


}
