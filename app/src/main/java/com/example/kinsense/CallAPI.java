package com.example.kinsense;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CallAPI  extends AsyncTask<Void, Void, Void> {

    private static String API = "https://app.kinsense.terenz.ai/process/";
    private static Context context; // remove this later after testing


        public CallAPI(Context context) {
            this.context = context;
        }


        public void getResponse(){

        }

        public static String getData () {
            String json = null;
            try {
                InputStream is = context.getAssets().open("test.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return json;

        }


        @Override
        protected Void doInBackground (Void...voids){

            try {
                URL url = new URL(API);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                //connection.setRequestProperty("Accept","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                JSONArray jsonArray = new JSONArray(getData());

                //write json to call
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(jsonArray.toString());
                dataOutputStream.close();
                dataOutputStream.flush();

                int responseCode = connection.getResponseCode();
                Log.d("Response Code: ", String.valueOf(responseCode));


                connection.disconnect();

            } catch (Exception e) {

                Log.d("Exception occured: ", "GOD KNOWS");
                e.printStackTrace();
            }
        return null;
        }

 }




