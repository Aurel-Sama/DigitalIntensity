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
import android.widget.Switch;
import android.widget.TextView;

import java.util.Random;

public class ValveFragment extends Fragment {
    private TextView valveTv;
    private RandomThread rt;
    private Switch openSwitch;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_valve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        valveTv = (TextView) view.findViewById(R.id.valve_tv);
        openSwitch = view.findViewById(R.id.valve_switch);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.aldrich);
        valveTv.setTypeface(typeface);
        //rt = new RandomThread(view,valveTv);
        //rt.setDaemon(true);
        //rt.start();
        setText(new Random());
        openSwitch.setOnCheckedChangeListener((view1, isChecked) ->{
            setText(isChecked);
        });



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
        //rt.setValue(false);
        super.onStop();
    }

    public void setText(Random rand){
        if(rand.nextInt(2)==0){
            valveTv.setTextColor(Color.RED);
            valveTv.setText("Etat de la valve : fermée");
            openSwitch.setChecked(false);

        }
        else{
            valveTv.setText("Etat de la valve : ouverte");
            openSwitch.setChecked(true);
        }

    }

    public void setText(Boolean bool){
        if(!bool){
            valveTv.setTextColor(Color.RED);
            valveTv.setText("Etat de la valve : fermée");

        }
        else{
            valveTv.setTextColor(Color.WHITE);
            valveTv.setText("Etat de la valve : ouverte");
        }

    }

}
