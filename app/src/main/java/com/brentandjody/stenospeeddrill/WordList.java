package com.brentandjody.stenospeeddrill;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by brentn on 08/09/14.
 */
public class WordList {

    private static final String TAG = WordList.class.getSimpleName();
    private List<String> wordlist = new LinkedList<String>();
    private boolean loaded;
    private Random rand;

    public WordList(Context context) {
        rand = new Random();
        loaded=false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("AmericanWordList.txt")));
            String word = reader.readLine();
            while (word != null) {
                wordlist.add(word);
                word = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading wordlist");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream");
                }
            }
        }
        loaded=true;
    }

    public String getWord() {
        if (!loaded) {
            Log.e(TAG, "Wordlist not loaded");
            return " - ";
        }
        return wordlist.get(rand.nextInt(wordlist.size()+1));
    }
}
