package com.brentandjody.stenospeeddrill;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private static final String TAG = Drill.class.getSimpleName();

    //instance variables
    private WordList wordlist;
    private long drill_start_time;
    private float accuracy;
    private TextView presentation_area;
    private boolean stopped;
    private Context mContext;

    public Drill(Context context,TextView presentation) {
        mContext = context;
        presentation_area = presentation;
        wordlist = new WordList(context);
        stopped=false;
        drill_start_time=new Date().getTime();
        accuracy=0;
    }

    public void run() {
        //display countdown timer, then begin
        final int COUNTDOWN_FROM=3;
        Handler handler = new Handler();
        for (Integer count=COUNTDOWN_FROM; count>0; count--) {
            Runnable countdown = display_countdown(count.toString());
            handler.postDelayed(countdown, (COUNTDOWN_FROM-count)*1000);
        }
        handler.postDelayed(main_drill_loop(), COUNTDOWN_FROM*1000);
    }

    public void stop() {
        stopped=true;
    }

    private boolean finished() {
        if (stopped) return true;
        long current_time = new Date().getTime();
        if ((current_time - drill_start_time)/1000 > DRILL_DURATION) return true;
        return false;
    }

    private List<String> getWords() {
        List<String> result = new ArrayList<String>();
        for (int i=0; i<PRESENTATION_WORDS; i++) {
            result.add(wordlist.getWord());
        }
        return result;
    }

    private void displayWords(List<String> words) {
        final StringBuilder output = new StringBuilder();
        for (String word : words) {
            output.append(word);
            output.append(" ");
        }
        Log.d(TAG, output.toString());
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presentation_area.setText(output.toString());
            }
        });
    }

    private Runnable main_drill_loop() {
        return new Runnable() {
            @Override
            public void run() {
                int frequency = 60000*PRESENTATION_WORDS/PRESENTATION_SPEED;
                presentation_area.setText("");
                final ScheduledExecutorService scheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
                scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        if (finished())
                            scheduleTaskExecutor.shutdown();
                        else
                            displayWords(getWords());
                    }
                }, 0, frequency, TimeUnit.MILLISECONDS);
            }
        };
    }

    private Runnable display_countdown(String t){
        final String text = t;
        return new Runnable() {
            @Override
            public void run() {
                Animation fadeout = new AlphaAnimation(0.1f, 0.0f);
                fadeout.setDuration(1000);
                presentation_area.setText(text);
                Log.d(TAG, text);
                presentation_area.startAnimation(fadeout);
            }
        };
    }
}
