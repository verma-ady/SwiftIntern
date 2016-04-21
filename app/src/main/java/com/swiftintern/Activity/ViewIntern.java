package com.swiftintern.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.swiftintern.Helper.AppController;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewIntern extends AppCompatActivity {


    TextView title, eligibility, category, duration, location, stipend;
    String myJSON;
    Button button;
    SharedPreferences sharedPreferences;
    String OrgID, OppID;
    private final String BASE = "http://swiftintern.com";
    private final String FIND_INTERN = "Home.json";
    private final String ORGANISATION = "organizations";
    private final String PHOTOS = "photo";
    private final String INTERN = "internship";
    private final String OPPORTUNITY = "opportunity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_intern);
        sharedPreferences = getApplicationContext().getSharedPreferences("UserInfo", getApplicationContext().MODE_PRIVATE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        Bundle extras = getIntent().getBundleExtra("Intern");
        myJSON = extras.getString("JSON");
//        name = (TextView) findViewById(R.id.text_name);
        title = (TextView) findViewById(R.id.text_title_name);
        eligibility = (TextView) findViewById(R.id.text_eligibility_name);
        category = (TextView) findViewById(R.id.text_category_name);
        duration = (TextView) findViewById(R.id.text_duration_name);
        location = (TextView) findViewById(R.id.text_location_name);
        stipend = (TextView) findViewById(R.id.text_payment_name);
        button = (Button) findViewById(R.id.button_viewintern_apply);

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

            collapsingToolbar.setTitle(jsonObjectoppurtunity.getString("_title"));
            eligibility.setText(jsonObjectoppurtunity.getString("_eligibility"));
            category.setText(jsonObjectoppurtunity.getString("_category"));
            duration.setText(jsonObjectoppurtunity.getString("_duration") );
            location.setText(jsonObjectoppurtunity.getString("_location"));
            stipend.setText(jsonObjectoppurtunity.getString("_payment"));
            OrgID = jsonObjectoppurtunity.getString("_organization_id");
            OppID = jsonObjectoppurtunity.getString("_id");
            setCompanyPic();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        applyButton();
    }

    private void setCompanyPic(){
        Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION).appendPath(PHOTOS).appendPath(OrgID).build();
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

        // If you are using normal ImageView
        imageLoader.get(uri.toString(), new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MyApp", "Image Load Error: " + error.getMessage());
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    CircleImageView iv = (CircleImageView) findViewById(R.id.imageView_viewIntern_company);
                    iv.setImageBitmap(response.getBitmap());
                }
            }
        });
    }

    public void applyButton(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("MyApp", getClass().toString() + " ApplyButton()");
                if (button.getContentDescription().equals("upload")) {
                    Intent intent = new Intent(ViewIntern.this, FilePickerActivity.class);
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
                Toast.makeText(getApplicationContext(), "Not a PDF File", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "File Size Larger than 9MB. Try Again", Toast.LENGTH_LONG);
                return;
            }
            String encode = encodeFileToBase64Binary(file);

            HTTPFileUpload hfu = new HTTPFileUpload("http://swiftintern.com/app/upload",
                    sharedPreferences.getString("token", null), encode, OppID , getApplicationContext());
            hfu.Send_Now();

        } catch (FileNotFoundException e ) {
            Log.v("MyApp", getClass().toString() +"Upload File Not Found");
            Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
            // Error: File not found
        } catch (IOException e ){
            Log.v("MyApp", getClass().toString() +"Upload IOException");
            Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getApplicationContext(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                if(jsonObject.getString("success").equals("true")){
                    Toast.makeText(getApplicationContext(), "Applied for Internship", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to applied for Internship", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }//getrepo
}
