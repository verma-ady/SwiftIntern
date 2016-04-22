package com.swiftintern.Fragment;

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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.swiftintern.Helper.AppController;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String ORGANISATIONS = "organizations";
    private final String ORGANISATION = "organization";
    private final String STUDENTS = "students";

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

        Uri uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS+".json").build();
        searchUserQual(uri.toString());

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

    private void searchUserQual(String url) {
        Log.d("MyApp", "searchQual URL" + url);
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "saveQual Response" + response);
                try {
                    progressDialog.dismiss();
                    JSONObject jsonObject = new JSONObject(response);
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

                    for(int i=0; i<length ; i++ ) {
                        Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATIONS)
                                .appendPath(ORGANISATION).appendPath("asdasd").appendPath(org_id[i] + ".json").build();
                        searchOrganisation(uri.toString(), i, length);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    textView.setText("No Qualifications Added");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "MyApp", "saveQual Error" + error.getMessage() );
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", sharedPreferences.getString("email", "") );
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

    private void searchOrganisation (String url, final int position, final int size ){
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("MyApp", response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    qualidata[position][0] = jsonObject.getJSONObject("organization").getString("_name");
                    cardsAppContent.addItem(new CardQualificationContent.DummyItem(qualidata[position][0], qualidata[position][1],
                            qualidata[position][2], qualidata[position][3],qualidata[position][4]));
                    if(position==(size-1)){
                        rvAdapter = new RVAdapter(cardsAppContent.ITEMS);
                        recyclerView.setAdapter(rvAdapter);
                        progressDialog.dismiss();
                    }
                } catch (JSONException exception ){
                    Log.e("MyApp", exception.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
                progressDialog.dismiss();
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
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
