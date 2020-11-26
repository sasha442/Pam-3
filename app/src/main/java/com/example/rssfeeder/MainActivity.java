package com.example.rssfeeder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rssfeeder.Adapter.FeedAdapter;
import com.example.rssfeeder.Common.HTTPDataHandler;
import com.example.rssfeeder.Model.RSSObject;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    DBHelper db;
    RecyclerView recyclerView;
    RSSObject rssObject;
    public String lastSelectedRss;
    public NetworkInfo activeNetworkInfo;
    String rssJsonString;
    public static final String SHARED_PREFS = "local_save";
    public ProgressBar progressBar;

    private String rssLink;
    private String toJsonApi = "https://api.rss2json.com/v1/api.json?rss_url=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu mMenu = navigationView.getMenu();
        db = new DBHelper(this);
        ArrayList<String> item = db.getAllRss();//getting data from database
        String[] lv_arr = new String[item.size()];//creating a String[] just as the size of the data retrieved from database

        //adding all data from item list to lv_arr[]
        for(int i=0;i<item.size();i++){
            lv_arr[i]= String.valueOf(item.get(i));
        }

        for(int i=0;i<lv_arr.length;i++) {
            mMenu.add(0, i, 0, lv_arr[i]).setShortcut('3','c');
        }

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);



        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


//        progressBar = findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.GONE);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                lastSelectedRss = item.getTitle().toString();
                SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                if (isNetworkAvailable() || sharedPreferences.contains(db.getLink(lastSelectedRss))) {
                    loadRSS(lastSelectedRss);
                } else {
                    Toast.makeText(getApplicationContext(), "No cached data",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void loadRSS(final String itemTitle) {
        @SuppressLint("StaticFieldLeak") AsyncTask<String,String,String> loadRSSAsync = new AsyncTask<String, String, String>() {

            ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
//            ProgressBar progressBar = new ProgressBar(MainActivity.this);

            @Override
            protected void onPreExecute() {
                mDialog.setMessage("Please wait...");
                mDialog.show();
//                progressBar = findViewById(R.id.progressBar);
//                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(String... params) {
                String result;
                HTTPDataHandler http = new HTTPDataHandler();
                result = http.GetHTTPData(params[0]);
                return  result;
            }

            @Override
            protected void onPostExecute(String s) {
                mDialog.dismiss();
//                progressBar.setVisibility(View.GONE);

                if (isNetworkAvailable()) {
                    saveData(getBaseContext(), s, db.getLink(itemTitle));
                } else {
                    s = loadData(getBaseContext(), db.getLink(itemTitle));
                    rssJsonString = s;
                }
                rssObject = new Gson().fromJson(s, RSSObject.class);
                FeedAdapter adapter = new FeedAdapter(rssObject,getBaseContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };

//        progressBar.setVisibility(View.GONE);
        rssLink = db.getLink(itemTitle);
        StringBuilder urlGetData = new StringBuilder(toJsonApi);
        urlGetData.append(rssLink);
        loadRSSAsync.execute(urlGetData.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.add_rss:Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", 0);

                Intent intent = new Intent(getApplicationContext(),AddRss.class);
                intent.putExtras(dataBundle);

                startActivity(intent);
                return true;
            case R.id.delete_rss:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.deleteRss)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.deleteRss(lastSelectedRss);
                                Toast.makeText(getApplicationContext(), "Deleted Successfully",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                AlertDialog d = builder.create();
                d.setTitle("Are you sure");
                d.show();
                return true;
            case R.id.clear_cash:
                SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                sharedPreferences.edit().clear().commit();
                Toast.makeText(getApplicationContext(), "The cache has been cleared",
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkAvailable() {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                // Update UI here when network is available.

                ConnectivityManager connectivityManager
                        = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (!(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting())) {
                    Toast.makeText(getApplicationContext(), "No internet connection",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

    public static void saveData(Context context, String rss, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, rss);
        editor.apply();
    }

    public static String loadData(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String text = sharedPreferences.getString(key, "");
        return text;
    }
}