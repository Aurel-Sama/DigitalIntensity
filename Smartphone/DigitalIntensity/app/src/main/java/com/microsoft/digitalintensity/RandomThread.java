package com.microsoft.digitalintensity;

import android.graphics.Color;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;

public class RandomThread extends Thread {
    View view;
    TextView tv;
    boolean value=true;


    public RandomThread(View view, TextView tv){
        this.view = view;
        this.tv = tv;
    }

    public void setValue(boolean value){
        this.value=value;
    }

    public void run(){
        Random rand = new Random();
        while(value){
            if(view.findViewById(R.id.fan_tv) !=null && tv.getId()==view.findViewById(R.id.fan_tv).getId()){
                MainThreadContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int maxSpeed = 2000;
                        int minSpeed = 400;
                        int fanSpeed = rand.nextInt(maxSpeed + 1 - minSpeed) + minSpeed;
                        ProgressBar pb = view.findViewById(R.id.fan_pb);
                        pb.setMax(maxSpeed);
                        pb.setProgress(fanSpeed);
                        if(fanSpeed > maxSpeed*0.75){
                            tv.setTextColor(Color.RED);
                            pb.getProgressDrawable().setColorFilter(
                                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        else{
                            tv.setTextColor(Color.WHITE);
                            pb.getProgressDrawable().setColorFilter(
                                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        tv.setText("Vitesse de rotation du ventilateur : "+fanSpeed+" rpm");
                    }
                });
            }
            else if(view.findViewById(R.id.sensor_tv) !=null && tv.getId()==view.findViewById(R.id.sensor_tv).getId()){
                MainThreadContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double maxVibrate = 5;
                        double precision = 10000;
                        double vibrateLevel = rand.nextDouble() * maxVibrate;
                        ProgressBar pb = view.findViewById(R.id.sensor_pb);
                        pb.setMax((int)(maxVibrate*2*precision));
                        pb.setProgress((int)(vibrateLevel*precision));
                        if(vibrateLevel > maxVibrate*0.75){
                            tv.setTextColor(Color.RED);
                            pb.getProgressDrawable().setColorFilter(
                                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);


                        }
                        else{
                            tv.setTextColor(Color.WHITE);
                            pb.getProgressDrawable().setColorFilter(
                                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);

                        }
                        tv.setText("Niveau de vibration : "+Math.round(vibrateLevel*100.)/100.+" dB");
                    }

                    });


            }
            else if(view.findViewById(R.id.pump_tv) !=null && tv.getId()==view.findViewById(R.id.pump_tv).getId()){
                MainThreadContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double maxTemperature = 200.;
                        double temperature = rand.nextDouble() * maxTemperature;
                        ProgressBar pb = view.findViewById(R.id.vertical_progressbar);
                        pb.setMax((int)maxTemperature);
                        pb.setProgress((int)temperature);
                        if(temperature > maxTemperature*0.75){
                            tv.setTextColor(Color.RED);
                            pb.getProgressDrawable().setColorFilter(
                                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        else{
                            tv.setTextColor(Color.WHITE);
                            pb.getProgressDrawable().setColorFilter(
                                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        tv.setText("Température de la pompe : "+Math.round(temperature*100.)/100.+" °C");
                    }
                });

            }
            else if(view.findViewById(R.id.valve_tv) !=null && tv.getId()==view.findViewById(R.id.valve_tv).getId()){
                MainThreadContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rand.nextInt(2)==0){
                            tv.setTextColor(Color.RED);
                            tv.setText("Etat de la valve : fermée");
                        }
                        else{
                            tv.setTextColor(Color.WHITE);
                            tv.setText("Etat de la valve : ouverte");
                        }
                    }
                });

            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        //thread in order to display information of the material
    }
}
