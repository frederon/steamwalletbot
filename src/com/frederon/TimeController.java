package com.frederon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

class TimeController extends Main{

    private int endId;
    private int startId;
    /*
    private Calendar lastRedeemTime;
    private Calendar nextRedeemTime;
    */
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public void sendEndId(int endId) {
        this.endId = endId;
    }

    public void sendStartId(int startId) {
        this.startId = startId;
    }

    public void startTimer(){
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                try {
                    System.out.println("It has been " + TIMERMINUTES + " minutes, continuing redeeming next steam wallet codes.");
                    System.out.println("Current time : " + dateFormat.format(Calendar.getInstance().getTime()));
                    timer.cancel();
                    startNextRedeem(startId, endId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        System.out.println("Current time : " + dateFormat.format(Calendar.getInstance().getTime()));
        if(!DEBUG) {
            timer.scheduleAtFixedRate(task, TIMERMINUTES * 60 * 1000,5000); //3 600 000 for 1 hour in delay
        } else {
            timer.scheduleAtFixedRate(task, 10000,5000); //Set timer to 10 seconds in debug mode
        }
    }

    /*
    public void sendLastRedeem(Calendar lastRedeemTime) {
        this.lastRedeemTime = lastRedeemTime;
        this.nextRedeemTime = this.lastRedeemTime;
        this.nextRedeemTime.add(Calendar.HOUR, 1);
    }
    */
}
