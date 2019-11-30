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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class CallAPI  extends AsyncTask<Void, Void, Void> {

    private static final String API = "https://app.kinsense.terenz.ai/process/";
    private static final String TAG = CallAPI.class.getSimpleName();
    public BufferedReader br ;
    public static String dateinstance ;
    private static Context context; // remove this later after testing


    public CallAPI(Context context, String dateinstance) {
            this.context = context;
            this.dateinstance = dateinstance;
        }


        public void getResponse(){

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

        public static JSONArray getJsonData() throws ParseException {
            JSONArray jsonArray = new JSONArray();
            String root = context.getExternalFilesDir(null).getAbsolutePath();
            File file = new File(root + "/test.json");
            Date date = new SimpleDateFormat("a hh:mm:ss").parse(dateinstance);

            int count = 1;
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String s;
                while( (s = br.readLine()) != null ){
                    int len = s.length();
                    if ( len < 62 )
                        continue;
                    else{
                        if(count > 32) {
                            count = 1;
                            date.setTime( date.getTime() + 1000);
                        }
                        JSONObject json = new JSONObject(s);
                        json.put("index", count);
                        json.put("Time", date.toString() );
                        count +=1;

                        //add to JsonArray
                        jsonArray.put(json);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e1){
                e1.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonArray;
        }


    @Override
        protected Void doInBackground (Void...voids){

            try {
                URL url = new URL(API);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                JSONArray jsonArray = getJsonData();


                JSONObject json = new JSONObject();
                json.put("data", jsonArray);


                Log.d(TAG, "request JSON last object: " + jsonArray.get(jsonArray.length()-1));

                //write json to call
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(json.toString());
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

                Log.d("Exception occured: ", "GOD KNOWS");
                e.printStackTrace();
            }
        return null;
        }

 }




