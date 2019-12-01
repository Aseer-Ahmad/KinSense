package com.example.kinsense;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;


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

public class CallAPI  extends AsyncTask<Void, Void, Void> {

    private static final String API = "https://app.kinsense.terenz.ai/process/";
    //private static final String API = "https://42337ae5.ngrok.io/process/";

    private static final String TAG = CallAPI.class.getSimpleName();
    public BufferedReader br ;
    public static String dateinstance ;
    private static Context context; // remove this later after testing
    private static JSONArray jsonArray;


    public CallAPI(Context context, String dateinstance, JSONArray  jsonArray) {
            this.context = context;
            this.dateinstance = dateinstance;
            this.jsonArray = jsonArray;
        }



        /*public static String getData () {
            String json = null;
            byte[] buffer =null;
            try {
                InputStream is = context.getAssets().open("TestJson.json");
                int size = is.available();
                Log.d(TAG, "json size: "+size);
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

            } catch (IOException e) {
                e.printStackTrace();
            }

            return json;

        }
        */
    @Override
        protected Void doInBackground (Void...voids){

            try {
                Log.d(TAG, "beginning async task... ");

                URL url = new URL(API);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                long startTime = System.nanoTime();
                long endTime = System.nanoTime();

                Log.d(TAG, "time taken to read JSON data from file: "+ (endTime - startTime)/1000000 + " ms");

                JSONObject json = new JSONObject();
                json.put("data", jsonArray);

                //writeJSONExternal( jsonArray.toString() ); //write to validate

                Log.d(TAG, "request JSONArray object size: " + json );

                //write json to call
                //BufferedWriter  dataOutputStream = new BufferedWriter( new OutputStreamWriter(connection.getOutputStream() , StandardCharsets.UTF_8));
                //dataOutputStream.write( json.toString() );

                DataOutputStream dataOutputStream = new DataOutputStream( connection.getOutputStream());
                dataOutputStream.writeBytes( json.toString() );
                dataOutputStream.close();
                dataOutputStream.flush();


                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: "+  responseCode );


                if(responseCode == 200){
                    String line;
                    br = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
                    while( (line = br.readLine()) != null){
                        if(line.isEmpty())
                            break;
                        Log.d(TAG, "Response from call: " + line );
                    }
                }


                connection.disconnect();

            } catch (Exception e) {

                Log.d(TAG, "GOD KNOWS what happened. HTTP Connection exception");
                e.printStackTrace();
            }
        return null;
        }

 }




