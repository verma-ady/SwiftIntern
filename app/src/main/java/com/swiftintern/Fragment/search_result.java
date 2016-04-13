package com.swiftintern.Fragment;

import android.app.ProgressDialog;
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
import java.util.List;

public class search_result extends Fragment {

    public search_result() {
        // Required empty public constructor
    }

    RecyclerView recyclerView;
    DummyContent dummyContent = new DummyContent();
    RVAdapter rvAdapter;
    TextView head;
    Integer pagenumber = 1, count, num;
    LinearLayoutManager linearLayoutManager;
    String myJSON, place, category, org_id[];
    ProgressDialog dialog_card, dialog_org, dialog_page;
    GetCompanyBitmapList getCompanyBitmapList = new GetCompanyBitmapList();

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
        head.setText(category + " Internships at " + place);

        dialog_card = new ProgressDialog(getActivity());
        dialog_card.setProgressStyle(android.R.attr.progressBarStyleSmall);
        dialog_card.setMessage("Connecting To SwiftIntern");
        dialog_card.show();

        try {
            JSONObject JSON = new JSONObject( myJSON);

            count = JSON.getInt("count");
            Log.v("MyApp", getClass().toString() +"NoIntern" + count);

            if(count==0){
                Log.v("MyApp", getClass().toString() +"NoIntern");
                dummyContent.addItem(new DummyContent.DummyItem("Sorry",
                        "No Internship available with selected category at selected place", null, null, null));
            }
            else {
                if ( count==10 || pagenumber!=(count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                    num=10;
                }
                else{
                    num=count%10;
                }


                org_id = new String[num];
                JSONArray opp = JSON.getJSONArray("opportunities");
                for (int i = 0; i < num; i++) {
                    JSONObject obj = opp.getJSONObject(i);

                    dummyContent.addItem(new DummyContent.DummyItem(obj.getString("_title"), obj.getString("_eligibility"),
                            obj.getString("_id"), obj.getString("_organization_id"), null));
                    org_id[i]= obj.getString("_organization_id");
                    Log.v("MyApp", getClass().toString() + "OrgID " + i + "  " + org_id[i] );
                }
            }
        } catch (JSONException e) {
            Log.v("MyApp", getClass().toString() +"search_result exception caught for JSON" );
            e.printStackTrace();
        }

        rvAdapter = new RVAdapter( dummyContent.ITEMS);
        recyclerView.setAdapter(rvAdapter);
        dialog_card.dismiss();

        RecyclerListener();

        if(count!=0) {
            getCompanyBitmapList = new GetCompanyBitmapList();
            getCompanyBitmapList.execute();
        }

        return view;
    }

    void RecyclerListener(){
        recyclerView.addOnItemTouchListener
                (new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                getCompanyBitmapList.cancel(true);
                                dialog_org = new ProgressDialog(getActivity());
                                dialog_org.setProgressStyle(android.R.attr.progressBarStyleSmall);
                                dialog_org.setMessage("Connecting To SwiftIntern");
                                dialog_org.show();
                                SearchOrganisation searchOrganisation = new SearchOrganisation();
                                searchOrganisation.execute(dummyContent.ITEMS.get(position).opp_id.toString());
                            }
                        })
                );

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (pagenumber != (count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                    pagenumber++;
                    SearchApi searchApi = new SearchApi();
                    searchApi.execute();
                    Log.v("MyApp", "Result next" + pagenumber);
                } else
                    Toast.makeText(getActivity(), "Already at last Page", Toast.LENGTH_SHORT).show();
            }
        });
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
            setAnimation(holder.itemView, position);
        }


        private void setAnimation(View viewToAnimate, int position)
        {
            // If the bound view wasn't previously displayed on screen, it's animated
            int first = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            int last = linearLayoutManager.findLastCompletelyVisibleItemPosition();

            if (position > last) // scroll down
            {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.pull_in_left);
                viewToAnimate.startAnimation(animation);
            } else if ( position<first ){ // scroll up
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.pull_in_right);
                viewToAnimate.startAnimation(animation);
            }
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

    public class SearchApi extends AsyncTask<String, Void, String > {

        @Override
        protected String doInBackground(String... params) {
            String error=null;
            if( params.length == 0 ){
                return "null_noInput";
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String base = "http://swiftintern.com";
            String find = "Home.json";
            URL url = null;
            try {

                Uri uri = Uri.parse(base).buildUpon().appendPath(find)
                        .appendQueryParameter("query", params[0])
                        .appendQueryParameter("location",params[1])
                        .appendQueryParameter("page", pagenumber.toString()).build();

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
            if( dialog_page!=null && dialog_page.isShowing() ) {
                Log.v( "MyApp", getClass().toString() +"Home page showing");
                dialog_page.dismiss();
            }

            if( dialog_card!=null && dialog_card.isShowing() ) {
                Log.v( "MyApp", getClass().toString() +"Home card showing");
                dialog_card.dismiss();
            }

            if( strJSON=="null_inputstream" || strJSON=="null_file" ){
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

//            Log.v("MyApp", getClass().toString() +"Result" + strJSON);

            try {
                dummyContent.ITEMS.clear();
                JSONObject JSON = new JSONObject( strJSON);
                JSONArray opp = JSON.getJSONArray("opportunities");

                if (pagenumber!=(count % 10 == 0 ? count / 10 : (count / 10) + 1)) {
                    num=10;
                }
                else{
                    num=count%10;
                }
                org_id = new String[num];
                for(int i=0; i<num ; i++ ){
                    JSONObject obj = opp.getJSONObject(i);
                    dummyContent.addItem(new DummyContent.DummyItem(obj.getString("_title"),
                            obj.getString("_eligibility"), obj.getString("_id"), obj.getString("_organization_id")
                            ,null));
                    org_id[i] = obj.getString("_organization_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            rvAdapter = (RVAdapter) recyclerView.getAdapter();
            rvAdapter = new RVAdapter(dummyContent.ITEMS);
            recyclerView.setAdapter(rvAdapter);
            getCompanyBitmapList = new GetCompanyBitmapList();
            getCompanyBitmapList.execute();
        }
    }

    public class SearchOrganisation extends AsyncTask<String, Void, String > {

        String LOG_CAT = "MyApp";
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

                Log.v(LOG_CAT,getClass().toString() + uri.toString());
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
                        Log.e(LOG_CAT, "ErrorClosingStream", e);
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
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }


//            Log.v("MyApp", getClass().toString() +"on post ");
//            Log.v("MyApp", "Result" + strJSON);

            ViewIntern viewIntern = new ViewIntern();
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment,viewIntern);
            fragmentTransaction.addToBackStack(null);

            Bundle stringJSON = new Bundle();
            stringJSON.putString("JSON", strJSON);

            viewIntern.setArguments(stringJSON);
            fragmentTransaction.commit();

//            try {
//                JSONObject JSON = new JSONObject(strJSON);
////                alert(JSON.getString("enddate"), JSON.getString("opportunity"), JSON.getString("organization"));
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        }
    }

    public class GetCompanyBitmapList extends AsyncTask<Void, Void, Bitmap> {

        public Bitmap myBitmap;
        public InputStream input = null;
        HttpURLConnection connection = null;

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                for( int i=0 ; i<num ; i++ ) {
                    URL url = new URL("http://swiftintern.com/organizations/photo/" + org_id[i]);
                    Log.v("MyApp", getClass().toString() +"   http://swiftintern.com/organizations/photo/" + org_id[i] );
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    input = connection.getInputStream();
                    myBitmap = BitmapFactory.decodeStream(input);
                    Log.v("MyApp", getClass().toString() +"Bitmap returned");
                    dummyContent.ITEMS.get(i).setBitmap(myBitmap);
                }
                return myBitmap;
            } catch ( IOException e ){
                e.printStackTrace();
            } finally {
                if( connection!=null ){
                    //no connection
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap b ) {
            if(b==null){
                Log.v("MyApp", getClass().toString() + "Bitmap Null");
                return;
            }

            rvAdapter = (RVAdapter) recyclerView.getAdapter();
            rvAdapter = new RVAdapter(dummyContent.ITEMS);
            recyclerView.setAdapter(rvAdapter);

        }
    }//getbitmap
}
