package com.swiftintern.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.swiftintern.Helper.HTTPFileUpload;
import com.swiftintern.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class ViewIntern extends Fragment {
    public ViewIntern() {
        // Required empty public constructor
    }
    TextView name, title, eligibility, category, duration, location, stipend;
    String myJSON;
    Button button;
    SharedPreferences sharedPreferences;
    String OrgID, OppID;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_view_intern, container, false);
        sharedPreferences = getContext().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.hold);

        Bundle extras = getArguments();
        myJSON = extras.getString("JSON");
        name = (TextView) view.findViewById(R.id.text_name);
        title = (TextView) view.findViewById(R.id.text_title_name);
        eligibility = (TextView) view.findViewById(R.id.text_eligibility_name);
        category = (TextView) view.findViewById(R.id.text_category_name);
        duration = (TextView) view.findViewById(R.id.text_duration_name);
        location = (TextView) view.findViewById(R.id.text_location_name);
        stipend = (TextView) view.findViewById(R.id.text_payment_name);
        button = (Button) view.findViewById(R.id.button_viewintern_apply);

        if(sharedPreferences.getString("resumeID", null)==null){
            button.setText("Upload Resume and Apply");
            button.setContentDescription("upload");
        } else {
            button.setText("Apply For Internship");
            button.setContentDescription("apply");
        }

        try {
            JSONObject jsonObjectmain = new JSONObject(myJSON);
            JSONObject jsonObjectorganisation = jsonObjectmain.getJSONObject("organization");
            JSONObject jsonObjectoppurtunity = jsonObjectmain.getJSONObject("opportunity");
            name.setText(jsonObjectorganisation.getString("_name"));
            title.setText(jsonObjectoppurtunity.getString("_title"));
            eligibility.setText(jsonObjectoppurtunity.getString("_eligibility"));
            category.setText(jsonObjectoppurtunity.getString("_category"));
            duration.setText(jsonObjectoppurtunity.getString("_duration"));
            location.setText(jsonObjectoppurtunity.getString("_location"));
            stipend.setText(jsonObjectoppurtunity.getString("_payment"));
            OrgID = jsonObjectoppurtunity.getString("_organization_id");
            OppID = jsonObjectoppurtunity.getString("_id");
            setCompanyPic();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        applyButton();
        return view;
    }

    private void setCompanyPic(){
        GetCompanyBitmap getCompanyBitmap = new GetCompanyBitmap();
        Log.v("MyApp", getClass().toString() + "GetBitmap URL: " + "http://swiftintern.com/organizations/photo/"+ OrgID );
        getCompanyBitmap.execute();
    }

    public class GetCompanyBitmap extends AsyncTask<Void, Void, Bitmap> {

        public Bitmap myBitmap;
        public InputStream input = null;
        HttpURLConnection connection = null;

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL url = new URL("http://swiftintern.com/organizations/photo/" + OrgID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
                Log.e("Bitmap", "returned");

            } catch ( IOException e ){
                e.printStackTrace();
            } finally {
                if( connection!=null ){
                    //no connection
                }
            }
            return myBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap b ) {
            ImageView iv = (ImageView) view.findViewById(R.id.imageView_viewIntern_company);
            iv.setImageBitmap(b);
            super.onPostExecute(b);
        }
    }//getbitmap

    public void applyButton(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("MyApp", getClass().toString() + " ApplyButton()");
                if (button.getContentDescription().equals("upload")) {
                    Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                    startActivityForResult(intent, 1);
                } else {
                    //directly apply
                    ApplyForInternship applyForInternship = new ApplyForInternship();
                    applyForInternship.execute();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String FilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH) ;
            Log.v("MyApp", getClass().toString() + " Upload FilePath: " + FilePath);
            String substr = FilePath.substring(FilePath.length() - 3);
            if(substr.equals("pdf")) {
                upload(FilePath);
            } else {
                Toast.makeText(getContext(), "Not a PDF File", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    public void upload (String dir){
        try {
            // Set your file path here
            Log.v("MyApp", getClass().toString() +"File Dir : " + dir);
            File file = new File(dir);
            if( file.length()/1048576 > 9 ){
                Log.v("MyApp", getClass().toString() + " File Size : " + file.length() );
                Toast.makeText(getContext(), "File Size Larger than 9MB. Try Again", Toast.LENGTH_LONG);
                return;
            }
            String encode = encodeFileToBase64Binary(file);

            HTTPFileUpload hfu = new HTTPFileUpload("http://swiftintern.com/app/upload",
                    sharedPreferences.getString("token", null), encode, OppID , getContext());
            hfu.Send_Now();

        } catch (FileNotFoundException e ) {
            Log.v("MyApp", getClass().toString() +"Upload File Not Found");
            Toast.makeText(getActivity(), "Upload Failed", Toast.LENGTH_SHORT).show();
            // Error: File not found
        } catch (IOException e ){
            Log.v("MyApp", getClass().toString() +"Upload IOException");
            Toast.makeText(getActivity(), "Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private static String encodeFileToBase64Binary(File fileName) throws IOException {
        byte[] bytes = new byte[(int)fileName.length()];
        FileInputStream fileInputStream = new FileInputStream(fileName);
        fileInputStream.read(bytes);
        fileInputStream.close();
        String encode = Base64.encodeToString(bytes, Base64.DEFAULT);
//        Log.v("MyApp", encode);
        return encode;
    }

    public class ApplyForInternship extends AsyncTask<Void, Void, String > {

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
                Log.v("MyApp", getClass().toString() + " token " + token  );
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
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                if(jsonObject.getString("success").equals("true")){
                    Toast.makeText(getContext(), "Applied for Internship", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Failed to applied for Internship", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }//getrepo
}
