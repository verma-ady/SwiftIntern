package com.swiftintern.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.swiftintern.Activity.ViewIntern;
import com.swiftintern.Helper.AppController;
import com.swiftintern.Helper.CardWorkContent;
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


public class ShowWorkDetails extends Fragment {
    public ShowWorkDetails() {
        // Required empty public constructor
    }

    RVAdapter rvAdapter;
    RecyclerView recyclerView;
    CardWorkContent cardWorkContent = new CardWorkContent();
    Button button;
    TextView textView;
    int length;
    String org_id[], workData[][], workID[], token;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String ORGANISATIONS = "organizations";
    private final String ORGANISATION = "organization";
    private final String WORK = "work";
    private final String STUDENTS = "students";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_work_details, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
        token= sharedPreferences.getString("token", null);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_work);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        button = (Button) view.findViewById(R.id.workdetail_add);
        textView = (TextView) view.findViewById(R.id.textWork);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Fetching Work Details");
        progressDialog.show();

        cardWorkContent.clear();

//        SearchUserWork searchUserWork = new SearchUserWork();
//        searchUserWork.execute();

        Uri uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS + ".json").build();
        searchUserWork( uri.toString() );

        CardListener();
        addmorework();
        return view;
    }

    public void CardListener(){
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {
                Log.v("MyApp", getClass().toString() + " Card Listener " + workID[position]);
                alertWork(workID[position]);
            }
        }));
    }

    private void alertWork(final String id ){
        new AlertDialog.Builder(getContext())
                .setTitle("Update Work Details")
                .setMessage("Are you sure you want to update this Work Detail?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with edit
                        WorkDetails workDetails = new WorkDetails();
                        android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, workDetails);
                        Bundle ID = new Bundle();
                        ID.putString("ID", id);
                        workDetails.setArguments(ID);
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

    public void addmorework(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkDetails workDetails = new WorkDetails();
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, workDetails);
                fragmentTransaction.commit();
            }
        });
    }

    private void searchUserWork (String url ) {
        Log.d("MyApp", "searchWork URL" + url);
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "saveWork Response" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray quali = jsonObject.getJSONArray("works");
                    length = quali.length();
                    org_id = new String[length];
                    workID = new String[length];
                    workData = new String[length][4];
                    for(int i=0; i<length ; i++ ){
                        JSONObject qualJSON = quali.getJSONObject(i);
                        workID[i] = qualJSON.getString("_id");
                        org_id[i] = qualJSON.getString("_organization_id");
                        workData[i][1] = qualJSON.getString("_duration");
                        workData[i][2] = qualJSON.getString("_designation");
                        workData[i][3] = qualJSON.getString("_responsibility");
                    }
//                    SearchOrganisationName searchOrganisationName = new SearchOrganisationName();
//                    searchOrganisationName.execute();

                    for(int i=0; i<length ; i++ ) {
                        Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATIONS)
                                .appendPath(ORGANISATION).appendPath("asdasd").appendPath(org_id[i] + ".json").build();
                        searchOrganisation(uri.toString(), i, length);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    textView.setText("No Work Records");
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "MyApp", "saveWork Response" + error.getMessage() );
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
                    workData[position][0] = jsonObject.getJSONObject("organization").getString("_name");
                    cardWorkContent.addItem(new CardWorkContent.DummyItem(workData[position][0], workData[position][2], workData[position][1] + " years", workData[
                            position][3]));
                    if(position==(size-1)){
                        rvAdapter = new RVAdapter(cardWorkContent.ITEMS);
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
                    workData[i][0] = jsonObject.getJSONObject("organization").getString("_name");
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
                Log.v("MyApp", getClass().toString() + Integer.toString(i) + "  " + workData[i][0] + "  " +
                        workData[i][1] + "  " + workData[i][2] + "  " + workData[i][3]);
                cardWorkContent.addItem(new CardWorkContent.DummyItem(workData[i][0], workData[i][2], workData[i][1] + " years", workData[i][3]));
            }
            rvAdapter = new RVAdapter(cardWorkContent.ITEMS);
            recyclerView.setAdapter(rvAdapter);
            progressDialog.dismiss();
        }
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {
        CardWorkContent cardWorkContent = new CardWorkContent();

        public RVAdapter ( List<CardWorkContent.DummyItem> list_cardsApp ){
            cardWorkContent.ITEMS = list_cardsApp;
        }

        @Override
        public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_work, parent, false );
            CardViewHolder cardViewHolder = new CardViewHolder( view );
            return cardViewHolder;
        }

        @Override
        public void onBindViewHolder(RVAdapter.CardViewHolder holder, int position) {
            holder.Org.setText(cardWorkContent.ITEMS.get(position).organisation);
            holder.Cat.setText(cardWorkContent.ITEMS.get(position).category);
            holder.Loc.setText(cardWorkContent.ITEMS.get(position).location);
            holder.Sti.setText(cardWorkContent.ITEMS.get(position).status);
        }

        @Override
        public int getItemCount() {
            return cardWorkContent.ITEMS.size();
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView Org, Cat, Loc, Sti;
            public CardViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cards_view_work);
                Org = (TextView) itemView.findViewById(R.id.text_card_work_company);
                Cat = (TextView) itemView.findViewById(R.id.text_card_work_designation);
                Loc = (TextView) itemView.findViewById(R.id.text_work_duration_name);
                Sti = (TextView) itemView.findViewById(R.id.text_work_respo_name);
            }
        }
    }
}
