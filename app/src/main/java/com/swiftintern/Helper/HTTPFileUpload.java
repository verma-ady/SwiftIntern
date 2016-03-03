package com.swiftintern.Helper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class HTTPFileUpload implements Runnable{
    URL connectURL;
    Context context;
    String Base64;
    String reply, token, OppID;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public HTTPFileUpload(String urlString, String stoken, String vbase64, String vOppID, Context vcontext){
        try{
            connectURL = new URL(urlString);
            Base64 = vbase64;
            context = vcontext;
            token = stoken;
            sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            OppID = vOppID;
            editor = sharedPreferences.edit();
        }catch(Exception ex){
            Log.v("MyApp", getClass().toString() +"URL Malformatted");
        }
    }

    public String Send_Now(){
        Sending();
        return reply;
    }

    void Sending(){
        upload up = new upload();
        try {
            up.execute().get();
        } catch (InterruptedException e) {
            Log.v("MyApp", getClass().toString() +"HTTPFileUpload Interrupted ");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.v("MyApp", getClass().toString() +"HTTPFileUpload Execution ");
            e.printStackTrace();
        }
    }

    public  class upload extends AsyncTask<Void, Void, String >{

        @Override
        protected String doInBackground(Void... params) {

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            String Tag="MyApp";

            try
            {
                Log.v(Tag, getClass().toString() +"Starting Http File Sending to URL");

                // Open a HTTP connection to the URL
                HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();

                // Allow Inputs
                conn.setDoInput(true);

                // Allow Outputs
                conn.setDoOutput(true);

                // Don't use a cached copy.
                conn.setUseCaches(false);

                // Use a post method.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("acess-token", token);
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(Base64);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                Log.v(Tag, getClass().toString() +"Headers are written");
                dos.flush();

                Log.v(Tag,getClass().toString() +"File Sent, Response: "+String.valueOf(conn.getResponseCode()));

                InputStream is = conn.getInputStream();

                // retrieve the response from server
                int ch;


                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                String s=b.toString();
                Log.v("MyApp", getClass().toString() + "Response" + s);
                dos.close();
                return s;
            }
            catch (MalformedURLException ex)
            {
                Log.v(Tag, getClass().toString() +"URL error: " + ex.getMessage(), ex);
                return "false";
            }

            catch (IOException ioe)
            {
                Log.v(Tag, getClass().toString() +"IO error: " + ioe.getMessage(), ioe);
                return "false";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s.equals("false")){
                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                return ;
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                if(jsonObject.getString("success").equals("true")){
                    Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
                    JSONObject resume = jsonObject.getJSONObject("resume");
                    editor.putString("resumeID", resume.getString("_id"));
                    editor.commit();
                    if(OppID!=null) {
                        ApplyForInternship applyForInternship = new ApplyForInternship();
                        applyForInternship.execute();
                    }
                } else {
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class ApplyForInternship extends AsyncTask<Void, Void, String > {

        //        String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(Void... params) {
            Log.v("MyApp", getClass().toString() + " Applying now for " + OppID );
            String error=null;
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com/app/apply.json";
            URL url = null;
            try {
                url= new URL(base);
                StringBuilder postDataString = new StringBuilder();

                postDataString.append(URLEncoder.encode("opportunity_id"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(OppID));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("resume_id"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sharedPreferences.getString("resumeID", null)));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("action"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode("internship"));

                byte[] postData = postDataString.toString().getBytes("UTF-8");

                int postDataLength = postData.length;

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                String token = sharedPreferences.getString("token", null);
                Log.v("MyApp", getClass().toString() + " token " + token);
                urlConnection.setRequestProperty("acess-token",token);

                urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(postDataLength));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(postData);

//                DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream());
//                try{
//                    wr.write( postData );
//                } catch (  )
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream==null){
                    return "null_inputstream";
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line ;

                while ( (line=bufferedReader.readLine())!=null ){
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return "null_inputstream";
                }

                String stringJSON = buffer.toString();
//                Log.v(LOG_CAT, stringJSON );
                return stringJSON;
            } catch (UnknownHostException | ConnectException e) {
                error = "null_internet" ;
                e.printStackTrace();
            } catch (IOException e) {
                error= "null_file";
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
//                        Log.e(LOG_CAT, "ErrorClosingStream", e);
                    }
                }
            }
            return error;
        }//doinbackground

        @Override
        protected void onPostExecute(String strJSON) {


            if( strJSON=="null_inputstream" || strJSON=="null_file" ){
                Toast.makeText(context, "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(context, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                if(jsonObject.getString("success").equals("true")){
                    Toast.makeText(context, "Applied for Internship", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Failed to applied for Internship", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }//getrepo

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }
}