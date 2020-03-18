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


public class SensorFragment extends Fragment {
    private TextView sensorTv;
    private RandomThread rt;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sensorTv = (TextView) view.findViewById(R.id.sensor_tv);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.aldrich);
        sensorTv.setTypeface(typeface);
        rt = new RandomThread(view,sensorTv);
        rt.setDaemon(true);
        rt.start();
        //setText(new Random());


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
        double maxVibrate = 5;
        double vibrateLevel = rand.nextDouble() * maxVibrate;
        if(vibrateLevel > maxVibrate*0.75){
            sensorTv.setTextColor(Color.RED);
        }
        sensorTv.setText("Niveau de vibration : "+Math.round(vibrateLevel*100.)/100.+" dB");

    }
}
