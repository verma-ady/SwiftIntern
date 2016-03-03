package com.swiftintern.Fragment;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftintern.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class studentprofile extends Fragment {

    public studentprofile() {
        // Required empty public constructor
    }

    TextView headername, info, name, email, phone, last, membership;
    SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_studentprofile, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        headername = (TextView) view.findViewById(R.id.text_headername);
        info = (TextView) view.findViewById(R.id.text_info);
        name = (TextView) view.findViewById(R.id.text_name_name);
        email = (TextView) view.findViewById(R.id.text_email_name);
        phone = (TextView) view.findViewById(R.id.text_phone_name);
        last = (TextView) view.findViewById(R.id.text_lastlogin_name);
        membership = (TextView) view.findViewById(R.id.text_membership_name);
        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);

        String head = sharedPreferences.getString("fname","") + " " + sharedPreferences.getString("lname", "");
        headername.setText(head);
        info.setText(sharedPreferences.getString("email", ""));
        search_user searchUser = new search_user();
        searchUser.execute();
        return view;
    }

    public class search_user extends AsyncTask<Void, Void, String > {

//        String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(Void... params) {

            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com/student.json";
            URL url = null;
            try {

                url= new URL(base);
                StringBuilder postDataString = new StringBuilder();
                postDataString.append(URLEncoder.encode("email"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sharedPreferences.getString("email", "")));
                byte[] postData = postDataString.toString().getBytes("UTF-8");

                int postDataLength = postData.length;

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                String token = sharedPreferences.getString("token", null);
                Log.v("MyApp", getClass().toString() +"studentProfile token " + token  );
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
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

//            dialog.dismiss();
            Log.v("MyApp", getClass().toString() +"on post : " + strJSON );
            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                JSONObject jsonObjectuser = jsonObject.getJSONObject("user");
                name.setText(headername.getText());
                email.setText(jsonObjectuser.getString("_email"));
                last.setText(jsonObjectuser.getString("_last_login"));
                phone.setText(jsonObjectuser.getString("_phone"));
                membership.setText(jsonObjectuser.getString("_type"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
