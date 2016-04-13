package com.swiftintern.Fragment;

import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
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

public class search extends Fragment {

    public search() {
        // Required empty public constructor
    }

    String places[] = {"Delhi", "Gurgaon", "Bangalore", "Mumbai"};

    View view;
    GridView gridView;
    TextView locationText;
    CardView locationCard;
    ProgressDialog dialog;
    String spinner_text, cat_intern;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);

        locationCard = (CardView) view.findViewById(R.id.getLocation);
        locationText = (TextView) view.findViewById(R.id.textLocation);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        gridView = (GridView) view.findViewById(R.id.gridView_category);
        gridView.setAdapter( new ImageAdapter( getActivity() ) );

        ArrayAdapter<String> list = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, places );

//        grid_view_listener();

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
                                        Toast.makeText(getContext(),"Selected Bangalore", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[2];
                                        locationText.setText(spinner_text);
                                        break;
                                    case R.id.bs_m:
                                        Toast.makeText(getContext(),"Selected Mumbai", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[3];
                                        locationText.setText(spinner_text);
                                        break;
                                    case R.id.bs_g:
                                        Toast.makeText(getContext(),"Selected Gurgaon", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[1];
                                        locationText.setText(spinner_text);
                                        break;
                                    case R.id.bs_d:
                                        Toast.makeText(getContext(),"Selected Delhi", Toast.LENGTH_SHORT).show();
                                        spinner_text = places[0];
                                        locationText.setText(spinner_text);
                                        break;
                                }
                            }
                        }).show();
            }
        });
    }

    void grid_view_listener(){
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                Toast.makeText( getActivity(), "Selected : " + category[position] + " and Place is " + spinner_places.getSelectedItem(),
//                                    Toast.LENGTH_SHORT ).show();

                String category[] = {"Web", "App", "Business", "Marketing", "Ambassador",
                        "Social", "Design", "Training"};
                cat_intern = category[position];
                search_api search = new search_api();

                dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
                dialog.setMessage("Connecting To SwiftIntern");
                dialog.show();
                search.execute(cat_intern, spinner_text);

            }
        });
    }

    public  class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int position) {
            return mThumbIds[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = (point.x)/2;

                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(width-15 , width-15));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        private int[] mThumbIds = {R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4,
                          R.drawable.a5, R.drawable.a6, R.drawable.a7, R.drawable.a8};
    }

    public class search_api extends AsyncTask<String, Void, String > {

//        String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(String... params) {

//            Log.v(LOG_CAT, "URL is " + "doInBackground");
            String error=null;
            if( params.length == 0 ){
                return "null_noInput";
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

//            String base = "https://api.github.com/users";
//            String repo= "repos";

            String base = "http://swiftintern.com";
            String find = "Home.json";
            URL url = null;
            try {

                Uri uri = Uri.parse(base).buildUpon().appendPath(find)
                        .appendQueryParameter("query", params[0])
                        .appendQueryParameter("location",params[1]).build();


                //url = new URL("https://api.github.com/users/verma-ady/repos");
//                Log.v(LOG_CAT, uri.toString());
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
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

//            dialog.dismiss();
//            Log.v("MyApp", getClass().toString() + "search + on post + " + strJSON );
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
