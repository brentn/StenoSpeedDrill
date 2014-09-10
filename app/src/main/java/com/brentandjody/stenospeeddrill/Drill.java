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
import java.util.concurrent.CountDownLatch;
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
    private static final int DRILL_DURATION = 10;     //end drill after this length of time (in seconds)
    private static final int PRESENTATION_WORDS = 5;   //how many words are displayed at a time?
    private static final int PRESENTATION_SPEED = 40;  //how quickly are new words displayed?
    private static final int SPEEDUP_INTERVAL = 0;     //how often will the speed increase by 5%? (in seconds)
    private static final int ACCURACY_THRESHOLD = 95;  //end drill when accuracy drops below this percentage

    private static final String TAG = Drill.class.getSimpleName();
    private static final int COUNTDOWN_FROM = 3;

    //instance variables
    private CountDownLatch latch;
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
        try {
            countdown();
            run_drill();
        } catch (InterruptedException e) {
            stopped=true;
        }
    }

    public void end() {
        stopped=true;
    }

    private boolean finished() {
        if (stopped) return true;
        long current_time = new Date().getTime();
        if ((current_time - drill_start_time)/1000 > DRILL_DURATION) return true;
        return false;
    }

    private void countdown() throws InterruptedException {
        Handler handler = new Handler();
        Animation fadeout = new AlphaAnimation(0.1f, 0.0f);
        fadeout.setDuration(1000);
        for (Integer i=COUNTDOWN_FROM; i>0; i--) {
            latch = new CountDownLatch(1);
            presentation_area.setText(i.toString());
            Log.d(TAG, i.toString());
            presentation_area.startAnimation(fadeout);
            handler.postDelayed(count_down(), 1000);
            latch.await();
        }
    }

    private List<String> getWords() {
        List<String> result = new ArrayList<String>();
        for (int i=0; i<PRESENTATION_WORDS; i++) {
            result.add(wordlist.getWord());
        }
        return result;
    }

    private int count_letters(List<String> wordlist) {
        if (wordlist==null) return 0;
        int result = 0;
        for (String word : wordlist) {
            result += word.length();
            result += 1; //add 1 for the space
        }
        result -=1; //remove trailing space
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

    private void run_drill() throws InterruptedException {
        Handler handler = new Handler();
        List<String> wordlist;
        drill_start_time = new Date().getTime();
        handler.postDelayed(end_drill(), DRILL_DURATION*1000);
        while (!finished()) {
            latch = new CountDownLatch(1);
            wordlist=getWords();
            displayWords(wordlist);
            int duration = (60000*count_letters(wordlist)/5)/PRESENTATION_SPEED;
            handler.postDelayed(count_down(), duration);
            latch.await();
        }
    }

    private Runnable count_down() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "countdown");
                latch.countDown();
            }
        };
    }
    private Runnable end_drill() {
        return new Runnable() {
            @Override
            public void run() {
                stopped=true;
            }
        };
    }
}
