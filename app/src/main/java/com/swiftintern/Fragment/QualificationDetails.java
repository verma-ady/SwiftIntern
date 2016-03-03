package com.swiftintern.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftintern.R;

import org.json.JSONArray;
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
import java.util.ArrayList;

public class QualificationDetails extends Fragment {

    public QualificationDetails() {
        // Required empty public constructor
    }

    View view;
    EditText degree, gpa, year, major;
    AutoCompleteTextView university;
    Button button;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<String> college = new ArrayList<>();
    ArrayAdapter<String> adapter;
    int count, page=1;
    SearchCollege searchCollege;
    String sUniversity, sDegree, sGpa, sYear, sMajor;
    String token, ID;
    ProgressDialog progressDialog;
    boolean update;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_qualification_details, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getActivity().MODE_PRIVATE);
        editor = getActivity().getSharedPreferences("UserInfo", getActivity().MODE_PRIVATE).edit();
        token = sharedPreferences.getString("token", null);
        Log.v("MyApp", getClass().toString() +"Token in QualificationDetails.class is " + token );

        try {
            Bundle extras = getArguments();
            ID = extras.getString("ID");
            Log.v("MyApp", getClass().toString() + " Extras: " + ID);
            update = true;
        } catch (Exception e){
            update = false;
        }

        university = (AutoCompleteTextView) view.findViewById(R.id.text_userinfo_university_name);
        degree = (EditText) view.findViewById(R.id.text_userinfo_degree_name);
        gpa = (EditText) view.findViewById(R.id.text_userinfo_gpa_name);
        year = (EditText) view.findViewById(R.id.text_userinfo_year_name);
        major = (EditText) view.findViewById(R.id.text_userinfo_major_name);
        button = (Button) view.findViewById(R.id.button_submit_userinfo);


        searchCollege = new SearchCollege();
        searchCollege.execute();
        fillCollege();
        keyevent();
        submit();

        return view;
    }

    public void keyevent(){
        year.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEND){
                    if(university.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter University", Toast.LENGTH_SHORT).show();
                    } else if(degree.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Degree", Toast.LENGTH_SHORT).show();
                    } else if(major.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Major", Toast.LENGTH_SHORT).show();
                    } else if(gpa.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Percentage", Toast.LENGTH_SHORT).show();
                    } else if(year.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Passing Year", Toast.LENGTH_SHORT).show();
                    } else {
                        sUniversity = university.getText().toString();
                        sDegree = degree.getText().toString();
                        sMajor = major.getText().toString();
                        sGpa = gpa.getText().toString();
                        sYear = year.getText().toString();
                        savequali();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void submit(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (university.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter University", Toast.LENGTH_SHORT).show();
                } else if (degree.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Degree", Toast.LENGTH_SHORT).show();
                } else if (major.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Major", Toast.LENGTH_SHORT).show();
                } else if (gpa.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Percentage", Toast.LENGTH_SHORT).show();
                } else if (year.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Passing Year", Toast.LENGTH_SHORT).show();
                } else {
                    sUniversity = university.getText().toString();
                    sDegree = degree.getText().toString();
                    sMajor = major.getText().toString();
                    sGpa = gpa.getText().toString();
                    sYear = year.getText().toString();
                    savequali();
                }
            }
        });
    }

    public void fillCollege(){
        while( page*500<count ){
            searchCollege = new SearchCollege();
            searchCollege.execute();
        }
    }

    void hidekeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void savequali(){
        hidekeyboard(view);
        SaveQuali saveQuali = new SaveQuali();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Saving Qualification");
        progressDialog.show();
        saveQuali.execute();
    }

    public class SaveQuali extends AsyncTask<Void, Void, String > {

        //        String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(Void... params) {
            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = null;

            if(update) {
                base = "http://swiftintern.com/students/qualification/" + ID +".json";
                Log.v("MyApp", getClass().toString() + "Update: " + Boolean.toString(update));
            } else {
                base = "http://swiftintern.com/students/qualification.json";
                Log.v("MyApp", getClass().toString() + "Update: " + Boolean.toString(update));
            }

            URL url = null;
            try {
                url= new URL(base);
                StringBuilder postDataString = new StringBuilder();
                postDataString.append(URLEncoder.encode("institute"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sUniversity));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("degree"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sDegree));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("major"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sMajor));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("gpa"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sGpa));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("passing_year"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sYear));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("action"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode("saveQual"));

                Log.v("MyApp", getClass().toString() + "post data " + postDataString);
                byte[] postData = postDataString.toString().getBytes("UTF-8");

                int postDataLength = postData.length;

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                urlConnection.setRequestProperty("acess-token",token);


                urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(postDataLength));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(postData);

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
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                if(jsonObject.getBoolean("success")){
                    Toast.makeText(getActivity(),"Qualification Saved Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(),"Unable to Save Qualification", Toast.LENGTH_SHORT).show();
                }
                ShowQualification showQualification = new ShowQualification();
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, showQualification);
                fragmentTransaction.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }//getrepo


    public class SearchCollege extends AsyncTask<Void, Void, String > {

        String LOG_CAT = "MyApp";

        @Override
        protected String doInBackground(Void... params) {
            String error = null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com";
            String find = "organizations.json";
            URL url = null;
            try {

                Uri uri = Uri.parse(base).buildUpon().appendPath(find)
                        .appendQueryParameter("page", Integer.toString(page))
                        .appendQueryParameter("type", "institute")
                        .appendQueryParameter("limit", "500").build();

//                Log.v("MyApp", getClass().toString() +"GetUserInfoSearch College URL : " + uri.toString());
                url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(isCancelled()){
                    return null;
                }

                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return "null_inputstream";
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return "null_inputstream";
                }

                String stringJSON = buffer.toString();
//                Log.v(LOG_CAT, stringJSON );
                return stringJSON;
            } catch (UnknownHostException | ConnectException e) {
//                Log.v("MyApp", "gothere");
                error = "null_internet";
                e.printStackTrace();
            } catch (IOException e) {
                error = "null_file";
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

            if(strJSON==null){
              return;
            } if (strJSON == "null_internet") {
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return;
            }
//            Log.v("MyApp", getClass().toString() +"on post ");
//            Log.v("MyApp", getClass().toString() +"GetUserInfoQualification College : " + strJSON);

            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                count = jsonObject.getInt("count");
                JSONArray jsonArray = jsonObject.getJSONArray("organizations");
                int i=0;
                while( jsonArray.getJSONObject(i)!=null ){
                    college.add(jsonArray.getJSONObject(i).getString("_name"));
                    i++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,college);
            university.setAdapter(adapter);
            university.setThreshold(1);
            page++;
            Log.v("MyApp", getClass().toString() +"GetUserInfoQualification : Done autocomplete " + page );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        searchCollege.cancel(true);
    }
}
