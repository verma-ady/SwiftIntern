package com.swiftintern.Fragment;

import android.app.ProgressDialog;
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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application extends Fragment {

    public Application() {
        // Required empty public constructor
    }

    View view;
    CardQualificationContent cardsAppContent = new CardQualificationContent();
    RVAdapter rvAdapter;
    RecyclerView recyclerView;
    SharedPreferences sharedPreferences;
    TextView textView;
    String token;
    String oppID[], AppData[][];
    int num;
    ProgressDialog dialog;
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String ORGANISATIONS = "organizations";
    private final String ORGANISATION = "organization";
    private final String OPPORTUNITY = "opportunity";
    private final String INTERNSHIP = "internship";
    private final String STUDENTS = "students";
    private final String APPLICATIONS = "applications";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_application, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_app);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        textView = (TextView) view.findViewById(R.id.text_app_head);

        token = sharedPreferences.getString("token", null);
//        cardsAppContent.addItem(new CardQualificationContent.DummyItem("org", "cat", "loc", "sti", "sta"));
        cardsAppContent.clear();
        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        dialog.setMessage("Fetching Applications");
        dialog.show();

        fill_cards_with_apps();

        rvAdapter = new RVAdapter(cardsAppContent.ITEMS);
        recyclerView.setAdapter(rvAdapter);

        return view;
    }

    private void fill_cards_with_apps(){
//        SearchApps searchApps = new SearchApps();
//        searchApps.execute();

        Uri uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS).appendPath(APPLICATIONS+ ".json").build();
        searchApp(uri.toString());

    }

    private void searchApp (String url){
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "searchApp:" + response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray application = jsonObject.getJSONArray("applications");
                    num = application.length();
                    oppID = new String[num];
                    AppData = new String[num][5];
                    JSONObject apps;
                    for(int i=0 ; i<num ; i++ ){
                        apps = application.getJSONObject(i);
                        oppID[i] = apps.getString("_opportunity_id");
                        AppData[i][1] = apps.getString("_status");
                    }
//                    SearchOrganisation searchOrganisation = new SearchOrganisation();
//                    searchOrganisation.execute();
                    for(int j=0; j<num ; j++ ) {
                        Uri uri = Uri.parse(BASE).buildUpon().appendPath(INTERNSHIP)
                                .appendPath(OPPORTUNITY).appendPath(oppID[j] + ".json").build();
                        searchOrganisation(uri.toString(), j, num);
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    textView.setText("No Applications");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
                dialog.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
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
                Log.d("MyApp", position+"  " + size+ "  "  + response.toString());
                try {
                    JSONObject main = new JSONObject(response);
                    JSONObject opportunity = main.getJSONObject("opportunity");
                    JSONObject organization = main.getJSONObject("organization");
                    AppData[position][0] = organization.getString("_name");
                    AppData[position][2] = opportunity.getString("_category");
                    AppData[position][3] = opportunity.getString("_location");
                    AppData[position][4] = opportunity.getString("_payment");
                    cardsAppContent.addItem(new CardQualificationContent.DummyItem(AppData[position][0], AppData[position][2],
                            AppData[position][3], AppData[position][4], AppData[position][1]));

                    if(position==(size-1)){
                        rvAdapter = new RVAdapter(cardsAppContent.ITEMS);
                        recyclerView.setAdapter(rvAdapter);
                        dialog.dismiss();
                    }
                } catch (JSONException exception ){
                    Log.e("MyApp", exception.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
                dialog.dismiss();
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
    }

    public class SearchOrganisation extends AsyncTask<Void, Void, String > {

        @Override
        protected String doInBackground(Void... params) {

            String error=null;
            String stringJSON=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com/internship/opportunity";
            String find;
            URL url = null;
            try {

                for(int i=0;i<num;i++) {
                    stringJSON = "false";
                    find = oppID[i] + ".json";
                    Uri uri = Uri.parse(base).buildUpon().appendPath(find).build();
                    Log.v("MyApp", getClass().toString() + "Search Opportunity: " + uri.toString());
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
                    JSONObject main = new JSONObject(stringJSON);
                    JSONObject opportunity = main.getJSONObject("opportunity");
                    JSONObject organization = main.getJSONObject("organization");
                    AppData[i][0] = organization.getString("_name");
                    AppData[i][2] = opportunity.getString("_category");
                    AppData[i][3] = opportunity.getString("_location");
                    AppData[i][4] = opportunity.getString("_payment");
//                    Log.v(LOG_CAT,getClass().toString() + stringJSON );
                    stringJSON="true";
                }
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
                dialog.dismiss();
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            if( strJSON.equals("false") ){
                dialog.dismiss();
                Toast.makeText(getActivity(), "Unable to connect to SwiftIntern", Toast.LENGTH_SHORT).show();
            }

            for(int i =0 ; i<num ; i++ ){
                Log.v("MyApp", getClass().toString() + Integer.toString(i) + "  " + AppData[i][0] + AppData[i][2] +
                        AppData[i][3] + AppData[i][4] + AppData[i][1]);
                cardsAppContent.addItem(new CardQualificationContent.DummyItem(AppData[i][0], AppData[i][2],
                        AppData[i][3], AppData[i][4], AppData[i][1]));
                rvAdapter = new RVAdapter(cardsAppContent.ITEMS);
                recyclerView.setAdapter(rvAdapter);
                dialog.dismiss();
            }
        }
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {
        CardQualificationContent cardsApp = new CardQualificationContent();

        public RVAdapter ( List<CardQualificationContent.DummyItem> list_cardsApp ){
            cardsApp.ITEMS = list_cardsApp;
        }

        @Override
        public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_for_applications, parent, false );
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
                cardView = (CardView) itemView.findViewById(R.id.cards_view_app);
                Org = (TextView) itemView.findViewById(R.id.text_card_app_org);
                Sta = (TextView) itemView.findViewById(R.id.text_card_app_sta);
                Loc = (TextView) itemView.findViewById(R.id.text_app_Loc_name);
                Sti = (TextView) itemView.findViewById(R.id.text_app_sti_name);
                Cat = (TextView) itemView.findViewById(R.id.text__app_cat_name);
            }
        }
    }

}
