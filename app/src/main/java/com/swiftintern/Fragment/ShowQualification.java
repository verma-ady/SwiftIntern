package com.swiftintern.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftintern.Fragment.QualificationDetails;
import com.swiftintern.Helper.CardQualificationContent;
import com.swiftintern.Helper.RecyclerItemClickListener;
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
import java.util.List;

public class ShowQualification extends Fragment {

    public ShowQualification() {
        // Required empty public constructor
    }

    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    TextView textView;
    RVAdapter rvAdapter;
    CardQualificationContent cardsAppContent = new CardQualificationContent();
    Button button;
    String token, org_id[], qualidata[][], QualiID[];
    int length;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_qualification, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
        token= sharedPreferences.getString("token", null);
        button =(Button) view.findViewById(R.id.qualidetails_add);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_quali);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        textView = (TextView) view.findViewById(R.id.textQualification);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Fetching Qualifications");
        progressDialog.show();

        cardsAppContent.clear();

//        cardsAppContent.addItem(new CardQualificationContent.DummyItem("Orga1", "cat1", "loc1", "sti1", "sta1"));
//        cardsAppContent.addItem(new CardQualificationContent.DummyItem("Orga2", "cat2", "loc2", "sti2", "sta2"));


        SearchUserQuali searchUserQuali = new SearchUserQuali();
        searchUserQuali.execute();

        cardlistener();
        addmorequali();
        return view;
    }

    public void cardlistener(){
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {
                Log.v("MyApp", getClass().toString() + " Card Listener " + QualiID[position]);
//                Toast.makeText(getContext(), workID[position], Toast.LENGTH_SHORT ).show();
                alertWork(QualiID[position]);
            }
        }));
    }

    private void alertWork(final String id ){
        new AlertDialog.Builder(getContext())
                .setTitle("Update Qualification Details")
                .setMessage("Are you sure you want to update this Qualification Detail?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with edit
                        QualificationDetails qualificationDetails = new QualificationDetails();
                        android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, qualificationDetails);
                        Bundle ID = new Bundle();
                        ID.putString("ID", id);
                        qualificationDetails.setArguments(ID);
                        fragmentTransaction.commit();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void addmorequali(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QualificationDetails qualificationDetails = new QualificationDetails();
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, qualificationDetails);
                fragmentTransaction.commit();
            }
        });
    }
    public class SearchUserQuali extends AsyncTask<Void, Void, String > {

                String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(Void... params) {

            Log.v(LOG_CAT, getClass().toString() + "doInBackground");
            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com/students.json";
            URL url = null;
            try {
                Log.v("MyApp", getClass().toString() +base);
                url= new URL(base);
                StringBuilder postDataString = new StringBuilder();
                postDataString.append(URLEncoder.encode("email"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sharedPreferences.getString("email", "")));
                byte[] postData = postDataString.toString().getBytes("UTF-8");

                int postDataLength = postData.length;

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                Log.v("MyApp", getClass().toString() + " Token is : " + token );
                urlConnection.setRequestProperty("acess-token",token);
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

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
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            try {
                progressDialog.dismiss();
                JSONObject jsonObject = new JSONObject(strJSON);
                JSONArray quali = jsonObject.getJSONArray("qualifications");
                length = quali.length();
                org_id = new String[length];
                QualiID = new String[length];
                qualidata = new String[length][5];
                for(int i=0; i<length ; i++ ){
                    JSONObject qualJSON = quali.getJSONObject(i);
                    org_id[i] = qualJSON.getString("_organization_id");
                    QualiID[i] = qualJSON.getString("_id");
                    qualidata[i][1] = qualJSON.getString("_degree");
                    qualidata[i][2] = qualJSON.getString("_major");
                    qualidata[i][3] = qualJSON.getString("_gpa");
                    qualidata[i][4] = qualJSON.getString("_passing_year");
                }
                SearchOrganisationName searchOrganisationName = new SearchOrganisationName();
                searchOrganisationName.execute();
            } catch (JSONException e) {
                e.printStackTrace();
                textView.setText("No Qualifications Added");
            }

            for(int i =0 ; i<length ; i++ ){
                Log.v("MyApp", getClass().toString() +"Organization IDs" + org_id[i]);
            }

//            Log.v("MyApp", getClass().toString() + strJSON);
        }
    }//getrepo

    public class SearchOrganisationName extends AsyncTask<Void, Void, String > {

                String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(Void... params) {

            Log.v(LOG_CAT, getClass().toString() + "   doInBackground");
            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com/organizations/organization/asdasd";

            URL url = null;
            try {
                String stringJSON = null;
                for(int i=0; i<length ; i++ ) {
                    Uri uri = Uri.parse(base).buildUpon().appendPath(org_id[i]+".json").build();
                    Log.v("MyApp", getClass().toString() + "URL: " + uri.toString() );
                    url = new URL(uri.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();

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

                    stringJSON = buffer.toString();

                    JSONObject jsonObject = new JSONObject(stringJSON);
                    qualidata[i][0] = jsonObject.getJSONObject("organization").getString("_name");
                }//endOfFor
//                Log.v(LOG_CAT, stringJSON );
                return stringJSON;
            } catch (UnknownHostException | ConnectException e) {
                error = "null_internet" ;
                e.printStackTrace();
            } catch (IOException e) {
                error= "null_file";
                e.printStackTrace();
            } catch (JSONException e) {
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
                progressDialog.dismiss();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return ;
            }

            for(int i =0 ; i<length ; i++ ){
                Log.v("MyApp", getClass().toString() + Integer.toString(i) + "  " + qualidata[i][0] + "  " +
                        qualidata[i][1] + "  " + qualidata[i][2] + "  "+ qualidata[i][3] + "  "+ qualidata[i][4]);
                cardsAppContent.addItem(new CardQualificationContent.DummyItem(qualidata[i][0], qualidata[i][1], qualidata[i][2],
                        qualidata[i][3],qualidata[i][4]));
                rvAdapter = new RVAdapter(cardsAppContent.ITEMS);
                recyclerView.setAdapter(rvAdapter);
                progressDialog.dismiss();
            }
        }
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {
        CardQualificationContent cardsApp = new CardQualificationContent();

        public RVAdapter ( List<CardQualificationContent.DummyItem> list_cardsApp ){
            cardsApp.ITEMS = list_cardsApp;
        }

        public void clear(){
            cardsApp.clear();
        }

        @Override
        public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_qualification, parent, false );
            CardViewHolder cardViewHolder = new CardViewHolder( view );
            return cardViewHolder;
        }

        @Override
        public void onBindViewHolder(RVAdapter.CardViewHolder holder, int position) {
            holder.Org.setText(cardsApp.ITEMS.get(position).organisation);
            holder.Cat.setText(cardsApp.ITEMS.get(position).category);
            holder.Loc.setText(cardsApp.ITEMS.get(position).location);
            holder.Sta.setText(cardsApp.ITEMS.get(position).status);
            holder.Sti.setText(cardsApp.ITEMS.get(position).stipend);
        }

        @Override
        public int getItemCount() {
            return cardsApp.ITEMS.size();
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView Org, Cat, Loc, Sti, Sta;
            public CardViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cards_view_quali);
                Org = (TextView) itemView.findViewById(R.id.text_card_quali_university);
                Cat = (TextView) itemView.findViewById(R.id.text_card_quali_degree);
                Loc = (TextView) itemView.findViewById(R.id.text_quali_major_name);
                Sti = (TextView) itemView.findViewById(R.id.text_quali_gpa_name);
                Sta = (TextView) itemView.findViewById(R.id.text__quali_year_name);
            }
        }
    }
}
