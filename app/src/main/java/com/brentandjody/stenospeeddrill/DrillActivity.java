package com.brentandjody.stenospeeddrill;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DrillActivity extends Activity {


    private Drill drill;
    private Button btnStart;
    private TextView presentation_text;
    private TextView countdown_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill);
        presentation_text = (TextView) findViewById(R.id.presentation_text);
        countdown_text = (TextView) findViewById(R.id.countdown);
        btnStart = (Button) findViewById(R.id.start_button);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drill = new Drill(DrillActivity.this, countdown_text, presentation_text);
                btnStart.setVisibility(View.INVISIBLE);
                drill.run();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (drill != null)
            drill.end();
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


}
