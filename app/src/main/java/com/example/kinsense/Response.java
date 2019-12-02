package com.example.kinsense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Response extends AppCompatActivity {


    private String response;
    private TextView textView;
    private final String TAG = Response.class.getSimpleName();

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        init();

        findComponents();

    }

    private void init() {
        Intent intent =getIntent();
        response = intent.getStringExtra("RESPONSE");
    }


    public void findComponents(){
        textView = findViewById(R.id.text_response);
        textView.setText(response);
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
