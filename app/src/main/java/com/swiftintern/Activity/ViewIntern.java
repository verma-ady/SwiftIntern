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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.swiftintern.Fragment.ShowQualification;
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
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewIntern extends AppCompatActivity {


    TextView title, eligibility, category, duration, location, stipend;
    String myJSON, token;
    Button button;
    SharedPreferences sharedPreferences;
    String OrgID, OppID;
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String ORGANISATION = "organizations";
    private final String PHOTOS = "photo";
    private final String APP = "app";
    private final String APPLY = "apply";


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

        token = sharedPreferences.getString("token", null);

        try {
            JSONObject jsonObjectMain = new JSONObject(myJSON);
            JSONObject jsonObjectoppurtunity = jsonObjectMain.getJSONObject("opportunity");

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
//                    ApplyForInternship applyForInternship = new ApplyForInternship();
//                    applyForInternship.execute();
                    Uri uri = Uri.parse(BASE).buildUpon().appendPath(APP).appendPath(APPLY + ".json").build();
                    applyIntern(uri.toString());
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

    private void applyIntern(String url){

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "applyIntern Response" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("success").equals("true")){
                        Toast.makeText(getApplicationContext(), "Applied for Internship", Toast.LENGTH_LONG).show();
                        button.setText("Applied For Internship");
                        button.setEnabled(false);
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to applied for Internship", Toast.LENGTH_LONG).show();
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

}
