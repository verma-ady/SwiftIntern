package com.swiftintern.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.swiftintern.Fragment.Application;
import com.swiftintern.Fragment.Home;
import com.swiftintern.Helper.HTTPFileUpload;
import com.swiftintern.R;
import com.swiftintern.Fragment.ShowQualification;
import com.swiftintern.Fragment.ShowWorkDetails;
import com.swiftintern.Fragment.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    search Search;
    com.swiftintern.Fragment.Home Home;
    Application application;
    ShowQualification showQualification;
    ShowWorkDetails showWorkDetails;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Home = new Home();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, Home);
        fragmentTransaction.commit();


        sharedPreferences = getApplicationContext().getSharedPreferences("UserInfo", getApplicationContext().MODE_PRIVATE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.update_resume) {
            Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String FilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH) ;
            Log.v("MyApp", getClass().toString() + " Upload FilePath: " + FilePath);
            String substr = FilePath.substring(FilePath.length() - 3);
            if(substr.equals("pdf")) {
                upload(FilePath);
            } else {
                Toast.makeText(getApplicationContext(), "Not a PDF File", Toast.LENGTH_LONG ).show();
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
                Toast.makeText(getApplicationContext(), "File Size Larger than 9MB. Try Again", Toast.LENGTH_LONG).show();
                return;
            }
            String encode = encodeFileToBase64Binary(file);

            HTTPFileUpload hfu = new HTTPFileUpload("http://swiftintern.com/app/upload",
                    sharedPreferences.getString("token", null), encode, null , getApplicationContext());
            hfu.Send_Now();

        } catch (FileNotFoundException e ) {
            Log.v("MyApp", getClass().toString() +"Upload File Not Found");
            Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_LONG).show();
            // Error: File not found
        } catch (IOException e ){
            Log.v("MyApp", getClass().toString() +"Upload IOException");
            Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_LONG).show();
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            Home = new Home();
            Toast.makeText(getApplicationContext(), "SwiftIntern", Toast.LENGTH_SHORT).show();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, Home);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_search) {
            Toast.makeText(getApplicationContext(), "Search for Internship", Toast.LENGTH_SHORT).show();
            Search = new search();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, Search);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, StudentProfile.class);
            startActivity(intent);

        } else if (id == R.id.nav_about_us) {
            Intent intent = new Intent(MainActivity.this, com.swiftintern.Activity.AboutUs.class);
            startActivity(intent);
        } else if (id == R.id.nav_app) {
            Toast.makeText(getApplicationContext(), "Applications", Toast.LENGTH_SHORT).show();
            application = new Application();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, application);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_work) {
            Toast.makeText(getApplicationContext(), "Work details", Toast.LENGTH_SHORT).show();
            showWorkDetails = new ShowWorkDetails();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, showWorkDetails);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_qualification) {
            Toast.makeText(getApplicationContext(), "Qualification Details", Toast.LENGTH_SHORT).show();
            showQualification = new ShowQualification();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, showQualification);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
