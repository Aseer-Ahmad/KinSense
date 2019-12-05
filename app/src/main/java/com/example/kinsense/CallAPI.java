package com.example.kinsense;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class CallAPI  extends AsyncTask<Void, Void, String> {

    private static final String API = "https://app.kinsense.terenz.ai/process/";
    //private static final String API = "https://42337ae5.ngrok.io/process/";

    private static final String TAG = CallAPI.class.getSimpleName();
    private BufferedReader br;
    private Context context; // remove this later after testing
    private String stringdata;
    //private ProgressBar progressBar = null;
    private ProgressDialog progressDialog = null;


    CallAPI(Context context,  String stringdata) {
            this.context = context;
            this.stringdata = stringdata;
        }

    @Override
    protected void onPreExecute() {

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setTitle("Fetching Results");

        progressDialog.setCancelable(false); //when back pressed
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

    }


    @Override
    protected void onPostExecute(String s) {
        progressDialog.dismiss();

        Intent intent = new Intent(context, Response.class);
        intent.putExtra("RESPONSE", s);
        context.startActivity(intent);
    }
/*
    public String getData () {
            String json = null;
            byte[] buffer =null;
            try {
                InputStream is = context.getAssets().open("TestJson.json");
                int size = is.available();
                Log.d(TAG, "json size: "+size);
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return json;

        }
*/
    @Override
        protected String doInBackground (Void...voids){

            try {
                Log.d(TAG, "beginning async task... ");

                URL url = new URL(API);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //long startTime = System.nanoTime();
                //long endTime = System.nanoTime();
                //Log.d(TAG, "time taken to read JSON data from file: "+ (endTime - startTime)/1000000 + " ms");

                /*
                JSONArray jsonArray = new JSONArray(getData()); //remove it after testing

                JSONObject json = new JSONObject();
                json.put("data", jsonArray);
                */

                Log.d(TAG, "Request string object : " + stringdata.length() );

                DataOutputStream dataOutputStream = new DataOutputStream( connection.getOutputStream());
                dataOutputStream.writeBytes( stringdata );
                dataOutputStream.flush();
                dataOutputStream.close();


                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: "+  responseCode );


                if(responseCode == 200){
                    String line;
                    br = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
                    while( (line = br.readLine()) != null){
                        if(line.isEmpty())
                            break;
                        Log.d(TAG, "Response from call: " + line );
                        return line;

                    }
                }

                connection.disconnect();

            } catch (Exception e) {

                Log.d(TAG, "GOD KNOWS what happened.");
                e.printStackTrace();
            }
        return null;
        }

 }




