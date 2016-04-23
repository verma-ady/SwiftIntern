package com.swiftintern.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
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

public class search_result extends Fragment {

    public search_result() {
        // Required empty public constructor
    }

    RecyclerView recyclerView;
    DummyContent dummyContent = new DummyContent();
    RVAdapter rvAdapter;
    TextView head;
    Integer pagenumber, count, num, cnt = 0, i, j;
    LinearLayoutManager linearLayoutManager;
    String myJSON, place, category;
    ArrayList<Integer> org_id = new ArrayList<>();

    private final String BASE = "http://swiftintern.com";
    private final String FIND_INTERN = "Home.json";
    private final String ORGANISATION = "organizations";
    private final String PHOTOS = "photo";
    private final String INTERN = "internship";
    private final String OPPORTUNITY = "opportunity";
    private final String VOLLEY_REQUEST = "string_req_home";

    @Override
    public void onResume() {
        super.onResume();
        Log.v("MyApp", getClass().toString() + "onResume");
        cnt = 0;
        j=0;
        num = 0;
        pagenumber  = 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        head = (TextView) view.findViewById(R.id.text_head);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        Bundle extras = getArguments();
        myJSON = extras.getString("JSON");
        place = extras.getString("place");
        category = extras.getString("category");
        if(place.equals("")){
            head.setText(category + " Internships");
        } else {
            head.setText(category + " Internships at " + place);
        }

        num=0;
        pagenumber = 1;
        j=0;
        int temp=0;
        try {
            JSONObject JSON = new JSONObject( myJSON);

            count = JSON.getInt("count");
            Log.v("MyApp", getClass().toString() +"NoIntern" + count);

            if(count==0){
                Log.v("MyApp", getClass().toString() +"NoIntern");
                dummyContent.addItem(new DummyContent.DummyItem("Sorry",
                        "No Internship available with selected category at selected place", null, null, null));
            } else {
                if ( count==10 || pagenumber!=(count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                    temp=10;
                } else{
                    temp=count%10;
                }

                JSONArray opp = JSON.getJSONArray("opportunities");
                for (int i = 0; i < temp; i++) {
                    JSONObject obj = opp.getJSONObject(i);

                    dummyContent.addItem(new DummyContent.DummyItem(obj.getString("_title"), obj.getString("_eligibility"),
                            obj.getString("_id"), obj.getString("_organization_id"), null));
                    org_id.add(obj.getInt("_organization_id"));
                }
            }
        } catch (JSONException e) {
            Log.v("MyApp", getClass().toString() +"search_result exception caught for JSON" );
            e.printStackTrace();
        }

        num = num + temp;
        rvAdapter = new RVAdapter( dummyContent.ITEMS);
        recyclerView.setAdapter(rvAdapter);

        RecyclerListener();


        for( ; j<num ; j++ ) {
            FillImages(j);
        }

        return view;
    }

    void RecyclerListener(){
        recyclerView.addOnItemTouchListener
                (new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                Uri uri = Uri.parse(BASE).buildUpon().appendPath(INTERN).appendPath(OPPORTUNITY)
                                        .appendPath(dummyContent.ITEMS.get(position).opp_id+".json").build();
                                SearchIntern(uri.toString());
                            }
                        })
                );

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if(pagenumber==1) num=10;
                if (pagenumber != (count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                    pagenumber++;
                    Log.v("MyApp", "Result next" + pagenumber);
                    Uri uri = Uri.parse(BASE).buildUpon().appendPath(FIND_INTERN)
                            .appendQueryParameter("query", category)
                            .appendQueryParameter("location", place)
                            .appendQueryParameter("page", pagenumber.toString()).build();

                    GetIntern(uri.toString());
                } else
                    Toast.makeText(getActivity(), "Already at last Page", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void GetIntern(String url) {
        Log.v("MyApp", "URL:" + url);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                FillIntern(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
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

        rvAdapter.notifyItemRangeInserted(num, dummyContent.ITEMS.size() - 1 );

        num = num + temp ;
        Log.v("MyApp", "DummyContent" + getClass().toString() + (num - temp) + " num is " + num);

        for( ; j<num ; j++ ) {
            FillImages(j);
        }

    }

    private void FillImages(final int pos){
        Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION).appendPath(PHOTOS).appendPath(org_id.get(pos).toString()).build();
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
                    Log.d("MyApp", "POS:" + pos);
                    dummyContent.ITEMS.get(pos).setBitmap(response.getBitmap());
                    rvAdapter.notifyItemChanged(pos);
                }
            }
        });
    }

    private void SearchIntern (String url){
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("MyApp", response.toString());
                Bundle stringJSON = new Bundle();
                stringJSON.putString("JSON", response.toString());
                Intent intent = new Intent (getActivity(), ViewIntern.class);
                intent.putExtra("Intern", stringJSON);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {
        DummyContent dummy = new DummyContent();

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
//            if(position>linearLayoutManager.findLastVisibleItemPosition()){//scroll down
//                holder.itemView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.bottum_up));
//            }
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
}