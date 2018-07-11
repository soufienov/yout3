package com.tutorialsface.yoump3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.tutorialsface.yoump3.adapter.CustomAdapter;
import com.tutorialsface.yoump3.controls.Controls;
import com.tutorialsface.yoump3.service.SongService;
import com.tutorialsface.yoump3.util.MediaItem;
import com.tutorialsface.yoump3.util.PlayerConstants;
import com.tutorialsface.yoump3.util.UtilFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
	String LOG_CLASS = "MainActivity";
	CustomAdapter customAdapter = null;
	static TextView playingSong;
	Button btnPlayer;
	static Button btnPause, btnPlay, btnNext, btnPrevious;
	Button btnStop;
	LinearLayout mediaLayout;
	static LinearLayout linearLayoutPlayingSong;
	ListView mediaListView;
	ProgressBar progressBar;
	TextView textBufferDuration, textDuration;
	static ImageView imageViewAlbumArt;
	static Context context;
	private String criteria="relevance";
	private String vidLen="any";
	private String maxres="30";
	String url;
	Intent playerIntent;
	String key="AIzaSyCE9NTL-TERQeWcz8M_VLGsqvCpNBo45FY";
	String[] keys= {"AIzaSyCE9NTL-TERQeWcz8M_VLGsqvCpNBo45FY","AIzaSyCHfCjmy0EyeHmCBtYQrai8Mt4SmNUklVA","AIzaSyDSVAuHx7ZQ6SRK3crmXlAeU1TGFIekpE8"};
	String title;
	private static final int MY_SOCKET_TIMEOUT_MS = 20000;
SearchView searchView;
	private AdView mAdView;
	private InterstitialAd mInterstitialAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);
		context = MainActivity.this;

		searchView=(SearchView)findViewById(R.id.searchView);    
		searchView.setQueryHint("Search Videos");
		mAdView = findViewById(R.id.adView);

		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-7106139341895351/4988118027");
		mInterstitialAd.loadAd(new AdRequest.Builder().build());

		searchView.setIconified(false);
		        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
				SendRequest(query);
				return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });

		try {
			init();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void init() throws JSONException {
		getViews();
		setListeners();
		playingSong.setSelected(true);
		progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_IN);
		if(PlayerConstants.SONGS_LIST.size() <= 0){
			PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
		}
    }

	private void setListItems() {
		customAdapter = new CustomAdapter(this,R.layout.custom_list, PlayerConstants.SONGS_LIST);
		mediaListView.setAdapter(customAdapter); 
		mediaListView.setFastScrollEnabled(true);
	}
	
	private void getViews() {
		playingSong = (TextView) findViewById(R.id.textNowPlaying);
		btnPlayer = (Button) findViewById(R.id.btnMusicPlayer);
		mediaListView = (ListView) findViewById(R.id.listViewMusic);
		mediaLayout = (LinearLayout) findViewById(R.id.linearLayoutMusicList);
		btnPause = (Button) findViewById(R.id.btnPause);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		linearLayoutPlayingSong = (LinearLayout) findViewById(R.id.linearLayoutPlayingSong);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		btnStop = (Button) findViewById(R.id.btnStop);
		textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
		textDuration = (TextView) findViewById(R.id.textDuration);
		imageViewAlbumArt = (ImageView) findViewById(R.id.imageViewAlbumArt);
		btnNext = (Button) findViewById(R.id.btnNext);
		btnPrevious = (Button) findViewById(R.id.btnPrevious);
	}

	private void setListeners() {
		 mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id){
            	Log.d("TAG", "TAG Tapped INOUT(IN)");
         		PlayerConstants.SONG_PAUSED = false;
         		PlayerConstants.SONG_NUMBER = position;
 				boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
 				if (!isServiceRunning) {
 					Intent i = new Intent(getApplicationContext(),SongService.class);
 					startService(i);
 				} else {
 					PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
 				}
 				updateUI();
 				changeButton();
            	Log.d("TAG", "TAG Tapped INOUT(OUT)");
            }
        });		
			
		btnPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this,AudioPlayerActivity.class);
				startActivity(i);
			}
		});
		btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.playControl(getApplicationContext());
			}
		});
		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.pauseControl(getApplicationContext());
			}
		});
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Controls.nextControl(getApplicationContext());
			}
		});
		btnPrevious.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Controls.previousControl(getApplicationContext());
			}
		});
		btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), SongService.class);
				stopService(i);
				linearLayoutPlayingSong.setVisibility(View.GONE);
			}
		});
		imageViewAlbumArt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this,AudioPlayerActivity.class);
				startActivity(i);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
		try{
	    	boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
			if (isServiceRunning) {
				updateUI();
			}else{
 				linearLayoutPlayingSong.setVisibility(View.GONE);
			}
			changeButton();
			PlayerConstants.PROGRESSBAR_HANDLER = new Handler(){
				 @Override
			      public void handleMessage(Message msg){
					 Integer i[] = (Integer[])msg.obj;
					 textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
					 textDuration.setText(UtilFunctions.getDuration(i[1]));
					 progressBar.setProgress(i[2]);
			    }
			};
		 }catch(Exception e){}
	}
	
	@SuppressWarnings("deprecation")
	public static void updateUI() {
		try{
			MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
			playingSong.setText(data.getTitle() + " " + data.getArtist() + "-" + data.getAlbum());
			Bitmap albumArt = UtilFunctions.getAlbumart(context, data.getAlbumId());
			if(albumArt != null){
				imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
			}else{
				imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
			}
			linearLayoutPlayingSong.setVisibility(View.VISIBLE);
		}catch(Exception e){}
	}
	
	public static void changeButton() {
		if(PlayerConstants.SONG_PAUSED){
			btnPause.setVisibility(View.GONE);
			btnPlay.setVisibility(View.VISIBLE);
		}else{
			btnPause.setVisibility(View.VISIBLE);
			btnPlay.setVisibility(View.GONE);
		}
	}
	
	public static void changeUI(){
		updateUI();
		changeButton();
	}
	private  void  SendRequest(String query){
//		progressBar.setVisibility(View.VISIBLE);

		RequestQueue queue = Volley.newRequestQueue(this);
		String url ="https://www.googleapis.com/youtube/v3/search?part=snippet&q="+query+"&type=video&order="+criteria+"&duration="+vidLen+"&maxResults="+maxres+"&key="+key;

// Request a string response from the provided URL.
		JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject  response)  {
						try {PlayerConstants.SONGS_LIST.clear(); JSONArray items=response.getJSONArray("items");
							int j;
							JSONObject item;
							MediaItem art;
							for ( j=0; j<items.length();j++){
								item= items.getJSONObject(j);
								art=new MediaItem(item);
								PlayerConstants.SONGS_LIST.add(art);
							}
							setListItems();
							if (mInterstitialAd.isLoaded()) {
								mInterstitialAd.show();
							}

							//progressBar.setVisibility(View.GONE);
						}
						catch (Exception e) {
							e.printStackTrace();
							//progressBar.setVisibility(View.GONE);

						}
					}}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d("That didn't work!","vbds");
			}
		});
// Add the request to the RequestQueue.
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
				MY_SOCKET_TIMEOUT_MS,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		queue.add(stringRequest);

	}

}