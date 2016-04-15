package com.swiftintern.Fragment;

import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.swiftintern.Helper.ContentSearchCategory;
import com.swiftintern.Helper.DummyContent;
import com.swiftintern.Helper.RecyclerItemClickListener;
import com.swiftintern.R;

import org.w3c.dom.Text;

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

public class search extends Fragment {

    public search() {
        // Required empty public constructor
    }

    String places[] = {"Delhi", "Gurgaon", "Bangalore", "Mumbai"};
    String category[] = {"Web", "App", "Business", "Marketing", "Ambassador",
            "Social", "Design", "Training"};


    View view;

    TextView locationText;
    CardView locationCard;
    ProgressDialog dialog;

    String spinner_text, cat_intern;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private RVAdapter rvAdapter;
    List<ContentSearchCategory.DummyItem> categoryList;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int layoutW;

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getContext(), 2 );
            int width = (point.x)/2;
            layoutW = width-40;
        }
        else{
            gridLayoutManager = new GridLayoutManager(getContext(), 3 );
            int width = (point.x)/3;
            layoutW = width-40;
        }
        recyclerView.setLayoutManager(gridLayoutManager);

        categoryList.clear();
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.web), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Web", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.app), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Application", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.business), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Business", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.marketing), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Marketing", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ambassador), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Ambassador", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.social), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Social", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.design), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Design", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.training), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Training", bitmap));

        rvAdapter = new RVAdapter(categoryList);
        recyclerView.setAdapter(rvAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);

        spinner_text = null;
        cat_intern = null;
        locationCard = (CardView) view.findViewById(R.id.getLocation);
        locationText = (TextView) view.findViewById(R.id.textLocation);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_category);
        recyclerView.setHasFixedSize(true);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int layoutW;

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getContext(), 2 );
            int width = (point.x)/2;
            layoutW = width-40;
        } else{
            gridLayoutManager = new GridLayoutManager(getContext(), 3 );
            int width = (point.x)/3;
            layoutW = width-40;
        }

        recyclerView.setLayoutManager(gridLayoutManager);
        categoryList = new ArrayList<>();

        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.web), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Web", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.app), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Application", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.business), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Business", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.marketing), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Marketing", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ambassador), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Ambassador", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.social), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Social", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.design), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Design", bitmap));

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.training), layoutW, layoutW, true);
        categoryList.add(new ContentSearchCategory.DummyItem("Training", bitmap));

        rvAdapter = new RVAdapter(categoryList);
        recyclerView.setAdapter(rvAdapter);

        cardNrecyclerListener();
        return view;
    }

    private void cardNrecyclerListener(){
        locationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomSheet.Builder(getActivity(), R.style.BottomSheet_StyleDialog).title("Select a Location").sheet(R.menu.search_bottomsheet)
                        .listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case R.id.bs_b:
                                        Toast.makeText(getContext(), "Selected Bangalore", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[2];
                                        locationText.setText(spinner_text);
                                        break;
                                    case R.id.bs_m:
                                        Toast.makeText(getContext(), "Selected Mumbai", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[3];
                                        locationText.setText(spinner_text);
                                        break;
                                    case R.id.bs_g:
                                        Toast.makeText(getContext(), "Selected Gurgaon", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[1];
                                        locationText.setText(spinner_text);
                                        break;
                                    case R.id.bs_d:
                                        Toast.makeText(getContext(), "Selected Delhi", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[0];
                                        locationText.setText(spinner_text);
                                        break;
                                }
                            }
                        }).show();
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                search_api search = new search_api();
                if(spinner_text==null){
                  locationCard.performClick();
                } else {
                    dialog = new ProgressDialog(getActivity());
                    dialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
                    dialog.setMessage("Connecting To SwiftIntern");
                    dialog.show();
                    cat_intern = category[position];
                    search.execute(cat_intern, spinner_text);
                }

            }
        }));

    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {

        ContentSearchCategory dummy= new ContentSearchCategory();
        public RVAdapter ( List<ContentSearchCategory.DummyItem> list_dummy ){
            dummy.ITEMS = list_dummy;
        }

        @Override
        public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_search_category, parent, false );
            CardViewHolder cardViewHolder = new CardViewHolder( view );
            return cardViewHolder;
        }

        @Override
        public void onBindViewHolder(RVAdapter.CardViewHolder holder, int position) {

            holder.text.setText(dummy.ITEMS.get(position).name);
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.imageView.setImageBitmap(dummy.ITEMS.get(position).pic);
        }

        @Override
        public int getItemCount() {
            return dummy.ITEMS.size();
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView text;
            ImageView imageView;
            public CardViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.card_category);
                text = (TextView) itemView.findViewById(R.id.card_category_text);
                imageView = (ImageView) itemView.findViewById(R.id.card_category_image);
            }
        }
    }

    public class search_api extends AsyncTask<String, Void, String > {

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
                        .appendQueryParameter("location",params[1]).build();

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


            if( strJSON=="null_inputstream" || strJSON=="null_file" ){
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            search_result searchResult = new search_result();
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment, searchResult);
            fragmentTransaction.addToBackStack(null);

            Bundle stringJSON = new Bundle();
            stringJSON.putString("JSON", strJSON);
            stringJSON.putString("place", spinner_text );
            stringJSON.putString("category", cat_intern  );
            searchResult.setArguments(stringJSON);
            dialog.dismiss();
            fragmentTransaction.commit();

        }
    }//getrepo
}
