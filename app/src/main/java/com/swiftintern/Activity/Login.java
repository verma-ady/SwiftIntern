package com.swiftintern.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.swiftintern.Helper.ConnectionDetector;
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
import java.util.Arrays;

public class Login extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener  {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;

    LoginButton loginButton;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    ProgressDialog dialog;
    FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            // App code
            Log.v("MyApp", getClass().toString() +"lb on success");
        }

        @Override
        public void onCancel() {
            Log.v("MyApp",getClass().toString() + "lb on cancel");
            // App code
        }

        @Override
        public void onError(FacebookException exception) {
            Log.v("MyApp",getClass().toString() + "lb on error");
            // App code
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        sharedPreferences = getApplicationContext().getSharedPreferences("UserInfo", getApplicationContext().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        dialog = new ProgressDialog(Login.this);
        dialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        dialog.setMessage("Login In Progress");

        if(sharedPreferences.getString("token",null)!=null){
            Log.v("MyApp", getClass().toString() + " token is not null " );
            dialog.dismiss();
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.button_login_fb).setOnClickListener(this);
        findViewById(R.id.button_login_gplus).setOnClickListener(this);

    }//oncreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Log.v("MyApp", getClass().toString() + " onActivityResult:If Google");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else if (requestCode==64206) {
            Log.v("MyApp", getClass().toString() + " onActivityResult:Facebook");
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
        dialog.show();
        switch(v.getId()){
            case R.id.button_login_fb:
                if( connectionDetector.isConnectingToInternet() ) {
                    loginButton = new LoginButton(Login.this);
                    accessTokenTracker = new AccessTokenTracker() {
                        @Override
                        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                            updateWithToken(newAccessToken);
                        }
                    };
                    callbackManager = CallbackManager.Factory.create();
                    loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
                    loginButton.registerCallback(callbackManager, callback);
                    loginButton.performClick();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.button_login_gplus:
                if( connectionDetector.isConnectingToInternet() ) {
                    // Build a GoogleApiClient with access to the Google Sign-In API and the
                    // options specified by gso.
                    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(new Scope(Scopes.PLUS_LOGIN)).requestEmail().build();
                    mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).addApi(Plus.API).build();
                    signIn();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // Google Sign In ---------------------------------------------------------------------------------------
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.v("MyApp", getClass().toString() + " handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            Log.v("MyApp", getClass().toString() + " Google handleSignInResult() if Login");
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if(mGoogleApiClient.hasConnectedApi(Plus.API)){
                Person person  = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                Log.v("MyApp", "--------------------------------");
                String a[] = person.getDisplayName().split(" ");

                try {
                    int sz = a.length;
                    if(sz>2) {
                        editor.putString("fname", a[0] + " " + a[1]);
                        editor.putString("lname", a[2]);
                    } else {
                        editor.putString("fname", a[0]);
                        editor.putString("lname", a[1]);
                    }
                    editor.putString("email", acct.getEmail());
                } catch (NullPointerException e ){
                    Log.v("MyApp", getClass().toString() + "handleSignInResult If NullPointerException" );
                }
            } else {
                try {
                    String a[] = acct.getDisplayName().split(" ");
                    int sz = a.length;
                    if(sz>2) {
                        editor.putString("fname", a[0] + " " + a[1]);
                        editor.putString("lname", a[2]);
                    } else {
                        editor.putString("fname", a[0]);
                        editor.putString("lname", a[1]);
                    }
                    editor.putString("email", acct.getEmail());
                } catch (NullPointerException e ){
                    Log.v("MyApp", getClass().toString() + "handleSignInResult Else NullPointerException" );
                }
            }

            editor.apply();
            SearchUser searchUser = new SearchUser();
            searchUser.execute();

        } else {
            // Signed out, show unauthenticated UI.
            Log.v("MyApp", getClass().toString() + "Google handleSignInResult() else Login " + result.getStatus() + "  "
                        + result.toString());
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Unable to Sign In", Toast.LENGTH_SHORT).show();
        }
    }
    //--------------------------------------------------------------------------------------- Google Sign In


    // FaceBook Sign In --------------------------------------------------------------------------------------
    private void updateWithToken( AccessToken currentAccessToken ){
        if(currentAccessToken!=null){
            Log.v("MyApp", getClass().toString() +"Login updateWithToken not null ");

            GraphRequest request = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.v("MyApp",getClass().toString() + response.toString());
                    // Get facebook data from Login
                    try {
                        Log.v("MyApp", getClass().toString() + object.toString());
                        Log.v("MyApp", getClass().toString() + object.getString("email"));

                        editor.putString("fname", object.getString("first_name"));
                        editor.putString("lname", object.getString("last_name"));
                        editor.putString("email", object.getString("email"));
                        editor.apply();

                        SearchUser searchUser = new SearchUser();
                        searchUser.execute();

                    } catch (JSONException e) {
                        Log.v("MyApp", getClass().toString() +"LoginJSON");
                        Toast.makeText(getApplicationContext(), "Unable to get your EMail-ID", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });

            Bundle parameters = new Bundle();
            // Parámetros que pedimos a facebook
            parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location");
            request.setParameters(parameters);
            request.executeAsync();

        } else {
            Log.v("MyApp", getClass().toString() + "Login updateWithToken null ");
            dialog.dismiss();
        }
    }//updatewithtoken

    //--------------------------------------------------------------------------------------- FaceBook Sign In

    public class SearchUser extends AsyncTask<Void, Void, String > {

        @Override
        protected String doInBackground(Void... params) {
            Log.v("MyApp", getClass().toString() + " Executing Login Procedure " );
            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String base = "http://swiftintern.com/app/student.json";
            URL url = null;
            try {
                url= new URL(base);
                StringBuilder postDataString = new StringBuilder();
                postDataString.append(URLEncoder.encode("fname"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sharedPreferences.getString("fname", null) +
                                                        sharedPreferences.getString("lname", null)));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("email"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sharedPreferences.getString("email", null)));

                byte[] postData = postDataString.toString().getBytes("UTF-8");

                int postDataLength = postData.length;

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

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
//                Log.v(LOG_CAT, getClass().toString() +stringJSON );
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
            Log.v("MyApp", getClass().toString() + " onPostExecute Response: " + strJSON );

            if ( strJSON.equals("null_internet") ){

                Toast.makeText(getApplicationContext(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                if(jsonObject.getBoolean("success")) {
                    JSONObject jsonObjectMeta = jsonObject.getJSONObject("meta");
                    JSONObject jsonObjectUser = jsonObject.getJSONObject("user");
                    editor.putInt("userID",jsonObjectUser.getInt("_id"));
                    Log.v("MyApp", getClass().toString() + "token in Login is " + jsonObjectMeta.getString("_meta_value"));
                    Log.v("MyApp", getClass().toString() + "User ID:" + jsonObjectUser.getInt("_id"));
                    editor.putString("token", jsonObjectMeta.getString("_meta_value"));
                    editor.apply();
                    dialog.dismiss();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }//SaveUser

}
