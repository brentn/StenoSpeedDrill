package com.brentandjody.stenospeeddrill;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
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
    private static final int DRILL_DURATION = 600;     //end drill after this length of time (in seconds)
    private static final int PRESENTATION_WORDS = 5;   //how many words are displayed at a time?
    private static final int INITIAL_SPEED = 40;  //how quickly are new words displayed?
    private static final int SPEEDUP_INTERVAL = 10;     //how often will the speed increase(in seconds)
    private static final int ACCURACY_THRESHOLD = 95;  //end drill when accuracy drops below this percentage

    private static final String TAG = Drill.class.getSimpleName();
    private static final int COUNTDOWN_FROM = 3;

    //instance variables
    private int presentation_speed;
    private WordList wordlist;
    private long drill_start_time;
    private int total_chars, errors;
    private TextView presentation_text, countdown_text, speed_text, accuracy_text, timer_text;
    private EditText input_text;
    private boolean finished;
    private DrillActivity activity;
    private String message;

    public Drill(Context context) {
        activity = (DrillActivity)context;
        presentation_text = (TextView) activity.findViewById(R.id.presentation_text);
        countdown_text = (TextView) activity.findViewById(R.id.countdown_text);
        input_text = (EditText) activity.findViewById(R.id.input_text);
        speed_text = (TextView) activity.findViewById(R.id.speed);
        accuracy_text = (TextView) activity.findViewById(R.id.accuracy);
        timer_text = (TextView) activity.findViewById(R.id.countdown);
        wordlist = new WordList(context);
        finished =false;
        presentation_speed = INITIAL_SPEED;
        drill_start_time=new Date().getTime();
        total_chars=0;
        errors=0;
        speed_text.setText(presentation_speed +" wpm");
        accuracy_text.setText("100%");
        timer_text.setText(DRILL_DURATION/60+":"+String.format("%02d", (DRILL_DURATION % 60)));
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
                    message = "Drill interrupted...";
                }
            }
        }).start();
    }

    public void end() {
        finished =true;
    }

    private void countdown() throws InterruptedException {
        for (Integer i=COUNTDOWN_FROM; i>0; i--) {
            final String text = i.toString();
            Log.d(TAG, text);
            setCountdownText(text);
            Thread.sleep(1000);
        }
        hideCountdown();
    }

    private void run_drill() throws InterruptedException {
        List<String> wordlist;
        drill_start_time = new Date().getTime();
        start_timer();
        while (!finished) {
            wordlist= getNewWords();
            displayWords(wordlist);
            int duration = (60000* total_letters(wordlist)/5)/ presentation_speed;
            Thread.sleep(duration);
            grade(cutFromInput(), wordlist);
            if ((new Date().getTime()-drill_start_time) > (DRILL_DURATION*1000)) {
                finished = true;
                message = "Drill Completed.";
            }
        }
        setPresentationText(message);
    }

    private void grade(final List<String> copy, final List<String> original) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                total_chars += total_letters(original);
                for (int i=0; i<PRESENTATION_WORDS; i++) {
                    if (copy.size() < (i+1)) { //there is no input for this item
                        errors += original.get(i).length()+1; //plus 1 for the space
                        Log.d(TAG, "no input for " + original.get(i) + "("+original.get(i).length()+")");
                    } else {
                        int difference = LevenshteinDistance(original.get(i), copy.get(i));
                        errors += difference;
                        Log.d(TAG, "orig:"+original.get(i)+" copy:"+copy.get(i)+" diff:"+difference);
                    }
                }
                int accuracy = Math.round(100 - (errors*100f/total_chars));
                setAccuracyText(Float.toString(accuracy)+"%");
                if (accuracy < ACCURACY_THRESHOLD) {
                    finished = true;
                    message = "Drill ended due to inaccuracy.";
                }
                Log.d(TAG, errors + "/" + total_chars);
            }
        }).start();
    }

    private void start_timer() {
        final long end_time = drill_start_time + (DRILL_DURATION * 1000);
        new Thread( new Runnable() {
            @Override
            public void run() {
                long now = new Date().getTime();
                long next_speedup = now+(SPEEDUP_INTERVAL*1000);
                while (! finished) {
                    now = new Date().getTime();
                    long time = (end_time - now) / 1000;
                    try {
                        Thread.sleep(1000);
                        setTimerText(time / 60 + ":" + String.format("%02d", (time % 60)));
                        if (now > next_speedup) {
                            presentation_speed += 1;
                            next_speedup = now+(SPEEDUP_INTERVAL*1000);
                        }
                    } catch (InterruptedException e) {
                        finished=true;
                        message="Timer interrupted.";
                    }
                }
            }
        }).start();
    }

    private List<String> getNewWords() {
        List<String> result = new ArrayList<String>();
        for (int i=0; i<PRESENTATION_WORDS; i++) {
            result.add(wordlist.getWord());
        }
        return result;
    }

    private List<String> cutFromInput() {
        List<String> result = new ArrayList<String>();
        for (String word : input_text.getText().toString().split(" ")) {
            result.add(word);
        }
        clearInputText();
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
        setPresentationText(output.toString());
    }

    public void hideCountdown() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countdown_text.setText("");
                countdown_text.setVisibility(View.GONE);
            }
        });
    }

    public void clearInputText() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                input_text.setText("");
            }
        });
    }

    public void setTimerText(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timer_text.setText(text);
            }
        });
    }

    public void setCountdownText(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Animation fadeout = new AlphaAnimation(0.1f, 0.0f);
                fadeout.setDuration(1000);
                countdown_text.setText(text);
                countdown_text.startAnimation(fadeout);
            }
        });
    }

    public void setPresentationText(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presentation_text.setText(text);
            }
        });
    }

    public void setAccuracyText(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                accuracy_text.setText(text);
            }
        });
    }

    public int LevenshteinDistance (String s0, String s1) {
        if (s0.equals(s1)) return 0;
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        for (int i = 0; i < len0; i++) cost[i] = i;
        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            for(int i = 1; i < len0; i++) {
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }
            int[] swap = cost; cost = newcost; newcost = swap;
        }
        return cost[len0 - 1];
    }
}
