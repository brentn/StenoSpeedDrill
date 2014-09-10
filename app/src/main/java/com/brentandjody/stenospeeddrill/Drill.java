package com.brentandjody.stenospeeddrill;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by brentn on 08/09/14.
 */
public class Drill {

    //Settings
    //TODO: move this into settings activity
    private static final String WORD_LIST = "";        //words to drill on (use internal list if blank)
    private static final int DRILL_DURATION = 30;     //end drill after this length of time (in seconds)
    private static final int PRESENTATION_WORDS = 5;   //how many words are displayed at a time?
    private static final int PRESENTATION_SPEED = 40;  //how quickly are new words displayed?
    private static final int SPEEDUP_INTERVAL = 0;     //how often will the speed increase by 5%? (in seconds)
    private static final int ACCURACY_THRESHOLD = 95;  //end drill when accuracy drops below this percentage

    private static final String TAG = Drill.class.getSimpleName();
    private static final int COUNTDOWN_FROM = 3;

    //instance variables
    private WordList wordlist;
    private long drill_start_time;
    private float accuracy;
    private TextView presentation_text;
    private TextView countdown_text;
    private boolean finished;
    private Context mContext;

    public Drill(Context context,TextView countdown, TextView presentation) {
        mContext = context;
        countdown_text = countdown;
        presentation_text = presentation;
        wordlist = new WordList(context);
        finished =false;
        drill_start_time=new Date().getTime();
        accuracy=0;
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    countdown();
                    run_drill();
                } catch (InterruptedException e) {
                    finished =true;
                }
            }
        }).start();
    }

    public void end() {
        finished =true;
    }

    private void countdown() throws InterruptedException {
        final Animation fadeout = new AlphaAnimation(0.1f, 0.0f);
        fadeout.setDuration(1000);
        for (Integer i=COUNTDOWN_FROM; i>0; i--) {
            final String text = i.toString();
            Log.d(TAG, text);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countdown_text.setText(text);
                    countdown_text.startAnimation(fadeout);
                }
            });
            Thread.sleep(1000);
        }
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countdown_text.setVisibility(View.GONE);
            }
        });
    }

    private void run_drill() throws InterruptedException {
        List<String> wordlist;
        drill_start_time = new Date().getTime();
        while (!finished) {
            wordlist=getWords();
            displayWords(wordlist);
            int duration = (60000* total_letters(wordlist)/5)/PRESENTATION_SPEED;
            Thread.sleep(duration);
            if ((new Date().getTime()-drill_start_time) > (DRILL_DURATION*1000))
                finished=true;
        }
        displayWords(null);
    }

    private List<String> getWords() {
        List<String> result = new ArrayList<String>();
        for (int i=0; i<PRESENTATION_WORDS; i++) {
            result.add(wordlist.getWord());
        }
        return result;
    }

    private int total_letters(List<String> wordlist) {
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
        if (words != null) {
            for (String word : words) {
                output.append(word);
                output.append(" ");
            }
        }
        Log.d(TAG, output.toString());
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presentation_text.setText(output.toString());
            }
        });
    }


}
