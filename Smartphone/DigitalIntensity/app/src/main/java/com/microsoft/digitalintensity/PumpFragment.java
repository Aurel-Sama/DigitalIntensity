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


public class PumpFragment extends Fragment {
    private TextView pumpTv;
    private RandomThread rt;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pump, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pumpTv = (TextView) view.findViewById(R.id.pump_tv);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.aldrich);
        pumpTv.setTypeface(typeface);
        rt = new RandomThread(view,pumpTv);
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
        double maxTemperature = 200;
        double temperature = rand.nextDouble() * maxTemperature;
        if(temperature > maxTemperature*0.75){
            pumpTv.setTextColor(Color.RED);
        }
        pumpTv.setText("Température de la pompe : "+Math.round(temperature*100.)/100.+" °C");

    }
}
