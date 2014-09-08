package com.brentandjody.stenospeeddrill;

import android.content.Context;

import java.util.Date;

/**
 * Created by brentn on 08/09/14.
 */
public class Drill {

    //Settings
    //TODO: move this into settings activity
    private static final String WORD_LIST = "";        //words to drill on (use internal list if blank)
    private static final int DRILL_DURATION = 300;     //end drill after this length of time (in seconds)
    private static final int PRESENTATION_WORDS = 5;   //how many words are displayed at a time?
    private static final int PRESENTATION_SPEED = 40;  //how quickly are new words displayed?
    private static final int SPEEDUP_INTERVAL = 0;     //how often will the speed increase by 5%? (in seconds)
    private static final int ACCURACY_THRESHOLD = 95;  //end drill when accuracy drops below this percentage

    //instance variables
    private long drill_start_time;
    private float accuracy;

    public Drill() {
        drill_start_time=new Date().getTime();
        accuracy=0;
    }

    public void run(Context context) {
        do_countdown(context);
        drill_start_time = new Date().getTime();
        while (! finished()) {

        }
    }

    private void do_countdown() {

    }

    private boolean finished() {
        long current_time = new Date().getTime();
        if ((current_time - drill_start_time)/1000 > DRILL_DURATION) return true;
        return false;
    }
}
