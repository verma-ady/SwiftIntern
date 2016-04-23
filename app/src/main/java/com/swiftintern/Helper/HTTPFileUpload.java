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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.swiftintern.Fragment.ShowQualification;
import com.swiftintern.R;

import org.json.JSONException;
import org.json.JSONObject;

public class HTTPFileUpload implements Runnable{

    URL connectURL;
    Context context;
    String Base64;
    String reply, token, OppID;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String APP = "app";
    private final String APPLY = "apply";

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

                Log.v(Tag, getClass().toString() + "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

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
                Log.v(Tag, getClass().toString() + "URL error: " + ex.getMessage(), ex);
                return "false";
            }

            catch (IOException ioe)
            {
                Log.v(Tag, getClass().toString() + "IO error: " + ioe.getMessage(), ioe);
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
                        Uri uri = Uri.parse(BASE).buildUpon().appendPath(APP).appendPath(APPLY + ".json").build();
                        applyIntern(uri.toString());
                    }
                } else {
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyIntern(String url){

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "applyIntern Response" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("success").equals("true")){
                        Toast.makeText(context, "Applied for Internship", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to applied for Internship", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "MyApp", "applyIntern Error" + error.getMessage() );
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("opportunity_id", OppID );
                params.put("resume_id", sharedPreferences.getString("resumeID", null));
                params.put("action", "internship");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Content-Language", "en-US");
                params.put("acess-token", token);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }
}