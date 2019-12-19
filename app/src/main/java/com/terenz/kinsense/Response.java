package com.terenz.kinsense;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Response extends Activity {


    private String response;
    private TextView textView_success;
    private TextView textView_result;
    private TextView textView_steps;
    private TextView textView_stepvelocity;
    private TextView textView_steptime;
    private TextView textView_steplength;
    private TextView textView_prediction;
    private TextView textView_stridevelocity;
    private TextView textView_stridelength;
    private TextView textView_stridetime;
    private TextView textView_responseaction;


    private JSONObject json;
    private String success;

    private final String TAG = Response.class.getSimpleName();

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        init();

        findComponents();

        showResults();

    }

    private void showResults() {

       Log.d(TAG, "displaying results") ;

        try {

            json = new JSONObject(response);
            success = json.getString("success") ;

            if(success.equals("true")){
                Log.d(TAG, "success true");

                JSONObject result = json.getJSONObject("result");
                Log.d(TAG, result.toString());
                //display results
                textView_success.setText( success.toUpperCase() );
                textView_success.setTextColor(Color.GREEN);
                textView_result.setText( "---" );

                textView_steps.setText( String.valueOf( result.getInt("steps")) );
                textView_stepvelocity.setText( String.valueOf(result.getDouble("step_velocity")) );
                textView_steptime.setText( String.valueOf(result.getDouble("step_time")));
                textView_steplength.setText( String.valueOf(result.getDouble("step_length")));

                textView_prediction.setText( result.getString("prediction"));

                textView_stridevelocity.setText( String.valueOf( result.getDouble("stride_veocity")));
                textView_stridetime.setText( String.valueOf( result.getDouble("stride_time")));
                textView_stridelength.setText( String.valueOf( result.getDouble( "stride_length")));

                textView_responseaction.setText("Want to try again. GO BACK");

            }else{
                Log.d(TAG, "success false");

                String result = json.getString("result");
                Log.d(TAG, result);
                //display results
                textView_success.setText( success.toUpperCase() );
                textView_success.setTextColor(Color.RED);
                textView_result.setText( result );

                textView_steps.setText( "---" );
                textView_stepvelocity.setText( "---" );
                textView_steptime.setText( "---" );
                textView_steplength.setText( "---" );

                textView_prediction.setText( "---" );

                textView_stridevelocity.setText( "---" );
                textView_stridetime.setText( "---" );
                textView_stridelength.setText( "---" );

                textView_responseaction.setText("NO RESULT!! GO BACK and PLEASE TRY AGAIN");


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void init() {
        Intent intent = getIntent();
        response = intent.getStringExtra("RESPONSE");
    }


    public void findComponents(){

       textView_success = findViewById(R.id.textview_success);
       textView_result = findViewById(R.id.textview_result);
       textView_steps = findViewById(R.id.textview_steps);
       textView_steplength = findViewById(R.id.textview_steplength);
       textView_steptime = findViewById(R.id.textview_steptime);
       textView_stepvelocity = findViewById(R.id.textview_stepvelocity);
       textView_prediction = findViewById(R.id.textview_prediction);
       textView_stridelength = findViewById(R.id.textview_stridelength);
       textView_stridetime = findViewById(R.id.textview_stridetime);
       textView_stridevelocity = findViewById(R.id.textview_stridevelocity);

       textView_responseaction = findViewById(R.id.textview_responseaction);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Activity stopped");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity paused");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destroyed");
    }
}
