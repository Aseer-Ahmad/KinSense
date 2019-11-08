package com.example.kinsense;

import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CallAPI extends AsyncTask<String, String, String> {

    private static String API = "https://app.kinsense.terenz.ai/process/";

    public CallAPI(){

        try {
            URL url = new URL(API);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected String doInBackground(String... strings) {
        return null;
    }

}
