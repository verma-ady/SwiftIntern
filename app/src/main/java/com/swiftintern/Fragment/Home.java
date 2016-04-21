package com.swiftintern.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.swiftintern.Activity.ViewIntern;
import com.swiftintern.Helper.AppController;
import com.swiftintern.Helper.DummyContent;
import com.swiftintern.Helper.EndlessRecyclerViewScrollListener;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {
    public Home() {
        // Required empty public constructor
    }

    private final String BASE = "http://swiftintern.com";
    private final String FIND_INTERN = "Home.json";
    private final String ORGANISATION = "organizations";
    private final String PHOTOS = "photo";
    private final String  tag_string_req = "string_req";

    RecyclerView recyclerView;
    DummyContent dummyContent = new DummyContent();
    RVAdapter rvAdapter;
    Integer pagenumber, i, j, count, num/*number of intern loaded*/;
    View view;
    ProgressDialog dialog_card, dialog_org, dialog_page, progressDialog;
    SharedPreferences sharedPreferences;
    ArrayList<Bitmap> companyBitmap;
    ArrayList<Integer> org_id;

    LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        sharedPreferences = getContext().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
        Log.v("MyApp", getClass().toString() +"Token is " + sharedPreferences.getString("token", "null"));
        companyBitmap = new ArrayList<>();
        org_id = new ArrayList<>();
        num = 0;
        pagenumber = 1;

        recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Connecting To SwiftIntern");
        progressDialog.show();

        rvAdapter = new RVAdapter();
        recyclerView.setAdapter(rvAdapter);
        Uri uri = Uri.parse(BASE).buildUpon().appendPath(FIND_INTERN)
                .appendQueryParameter("page", pagenumber.toString()).build();

        getIntern(uri.toString());
        RecyclerListener();

        return view;
    }

    private void getIntern (String url) {

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("MyApp", response.toString());
                FillIntern(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
                progressDialog.dismiss();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void FillIntern (String strJSON) {
        int temp=0;
        try {
            JSONObject JSON = new JSONObject( strJSON);
            JSONArray opp = JSON.getJSONArray("opportunities");
            count = JSON.getInt("count");

            if (pagenumber!=(count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                temp = 10;
            }
            else{
                temp = count%10;
            }
            for(i=0 ; i<temp ; i++ ){
                JSONObject obj = opp.getJSONObject(i);
                dummyContent.addItem(new DummyContent.DummyItem(obj.getString("_title"),
                        obj.getString("_eligibility"), obj.getString("_id"), obj.getString("_organization_id"), null));
                org_id.add(obj.getInt("_organization_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(pagenumber==1) {
            rvAdapter = (RVAdapter) recyclerView.getAdapter();
            rvAdapter.dummy.ITEMS = dummyContent.ITEMS;
            recyclerView.setAdapter(rvAdapter);
        } else {
//            Log.v("MyApp", getClass().toString() + " notified after loading interns data" );
            rvAdapter.notifyItemRangeInserted(num, dummyContent.ITEMS.size()-1 );
        }
        num = num + temp ;
        Log.v("MyApp", getClass().toString() + (num-temp) + " num is " + num );
//        getCompanyBitmapList = new GetCompanyBitmapList();
//        getCompanyBitmapList.execute();
        if( progressDialog!=null && progressDialog.isShowing() ) {
            progressDialog.dismiss();
        }
        FillImages();

    }

    private void FillImages(){
        Log.v("MyApp", "Out Num: "+num+ " J:" + j );
        for( ; j<num ; j++ ) {
            Log.v("MyApp", "In Num: "+num+ " J:" + j );
            Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION).appendPath(PHOTOS).appendPath(org_id.get(j).toString()).build();
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
                        Log.v("MyApp", "Num: "+num+ " J:" + j );
                        dummyContent.ITEMS.get(j).setBitmap(response.getBitmap());
                        rvAdapter.notifyItemChanged(j);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("MyApp", "onResume home");
        j=0;
        num = 0;
        pagenumber  = 1;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("MyApp", "onPause home");
    }

    void RecyclerListener(){
        recyclerView.addOnItemTouchListener
                (new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        SearchOrganisation searchOrganisation = new SearchOrganisation();
                        searchOrganisation.execute(dummyContent.ITEMS.get(position).opp_id);
                    }
                }
                ));

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (pagenumber != (count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                    pagenumber++;
                    Uri uri = Uri.parse(BASE).buildUpon().appendPath(FIND_INTERN)
                            .appendQueryParameter("page", pagenumber.toString()).build();
                    getIntern(uri.toString());
                } else
                    Toast.makeText(getActivity(), "Already at last Page", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {
        DummyContent dummy = new DummyContent();
        public RVAdapter(){
            //empty constructor
        }
        public RVAdapter ( List<DummyContent.DummyItem> list_dummy ){
            dummy.ITEMS = list_dummy;
        }

        @Override
        public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_for_search, parent, false );
            CardViewHolder cardViewHolder = new CardViewHolder( view );
            return cardViewHolder;
        }

        @Override
        public void onBindViewHolder(RVAdapter.CardViewHolder holder, int position) {
            holder.text.setText(dummy.ITEMS.get(position).id);
            holder.subtext.setText(dummy.ITEMS.get(position).content);
            holder.imageView.setImageBitmap(dummy.ITEMS.get(position).image);
        }

        @Override
        public void onViewDetachedFromWindow(CardViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public int getItemCount() {
            return dummy.ITEMS.size();
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView text, subtext;
            ImageView imageView;
            public CardViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cards_view);
                text = (TextView) itemView.findViewById(R.id.text_cards);
                subtext = (TextView) itemView.findViewById(R.id.subtext_cards);
                imageView = (ImageView) itemView.findViewById(R.id.image_for_search);
            }
        }
    }



    public class SearchOrganisation extends AsyncTask<String, Void, String > {

        //        String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(String... params) {
            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com/internship/opportunity";
            String find;
            URL url = null;
            try {

                find = params[0] + ".json";
                Uri uri = Uri.parse(base).buildUpon().appendPath(find).build();

                url= new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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
//                Log.v(LOG_CAT,getClass().toString() + stringJSON );
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
            if(dialog_org!=null && dialog_org.isShowing()) {
                Log.v("MyApp", getClass().toString() +"Home org showing" );
                dialog_org.dismiss();
            }
            if( strJSON=="null_inputstream" || strJSON=="null_file" ){
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

//            ViewIntern viewIntern = new ViewIntern();
//            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
//            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.add(R.id.fragment,viewIntern);
//            fragmentTransaction.addToBackStack(null);

            Bundle stringJSON = new Bundle();
            stringJSON.putString("JSON", strJSON);
            Intent intent = new Intent (getActivity(), ViewIntern.class);
            intent.putExtra("Intern", stringJSON);
            startActivity(intent);
        }
    }

}
