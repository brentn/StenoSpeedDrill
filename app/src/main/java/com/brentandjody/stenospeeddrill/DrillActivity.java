package com.brentandjody.stenospeeddrill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.TextView;

import java.util.Locale;


public class DrillActivity extends Activity implements OnInitListener {


    private Drill drill;
    Button btnStart;
    private int DATA_CHECK_CODE = 0;
    private TextToSpeech TTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);
        setContentView(R.layout.activity_drill);
        final TextView presentation = (TextView) findViewById(R.id.presentation_text);
        btnStart = (Button) findViewById(R.id.start_button);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drill = new Drill(DrillActivity.this);
                btnStart.setVisibility(View.INVISIBLE);
                presentation.setText("");
                drill.run();
            }
        });
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            TTS.setLanguage(Locale.US);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (drill != null)
            drill.end();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TTS.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drill, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                TTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void speak(String word) {
        TTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void silence() {
        TTS.stop();
    }
}
