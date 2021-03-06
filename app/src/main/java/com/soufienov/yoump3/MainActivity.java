package com.soufienov.yoump3;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.ConnectivityManager;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
import com.soufienov.yoump3.adapter.CustomAdapter;
import com.soufienov.yoump3.controls.Controls;
import com.soufienov.yoump3.service.SongService;
import com.soufienov.yoump3.util.MediaItem;
import com.soufienov.yoump3.util.PlayerConstants;
import com.soufienov.yoump3.util.UtilFunctions;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity  {
	String LOG_CLASS = "MainActivity";
	CustomAdapter customAdapter = null;
	static TextView playingSong;
	Button btnPlayer;
	public static Button btnDownload;
	static Button btnPause, btnPlay, btnNext, btnPrevious;
	Button btnStop,btnSearch;
	LinearLayout mediaLayout;
	static LinearLayout linearLayoutPlayingSong;
	ListView mediaListView;
	ProgressBar progressBar;
	TextView textBufferDuration, textDuration;
	static ImageView imageViewAlbumArt;
	static Context context;
	private String criteria="relevance";
	private String vidLen="any";
	private String maxres=PlayerConstants.MAXRES;
	String url;
	Intent playerIntent;
	String key="AIzaSyCE9NTL-TERQeWcz8M_VLGsqvCpNBo45FY";
	String[] keys= {"AIzaSyCE9NTL-TERQeWcz8M_VLGsqvCpNBo45FY","AIzaSyCHfCjmy0EyeHmCBtYQrai8Mt4SmNUklVA","AIzaSyDSVAuHx7ZQ6SRK3crmXlAeU1TGFIekpE8"};
	String title;
	private static final int MY_SOCKET_TIMEOUT_MS = 20000;
SearchView searchView;
	private AdView mAdView;
	private InterstitialAd mInterstitialAd;
FloatingActionMenu materialDesignFAM;
FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;
	public static AlphaAnimation outAnimation;
	public static FrameLayout progressBarHolder;
	public static AlphaAnimation inAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getActionBar().hide();
		setContentView(R.layout.activity_main);
		context = MainActivity.this;
		try {
			init();

		} catch (JSONException e) {
			e.printStackTrace();
		}
		mAdView = findViewById(R.id.adView);

		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-7106139341895351/4988118027");
		mInterstitialAd.loadAd(new AdRequest.Builder().build());



	}
	
	private void init() throws JSONException {
		getViews();
		setListeners();
		ActionBarTitleGravity();
		playingSong.setSelected(true);
		progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_IN);
		Log.e("size","size="+PlayerConstants.SONGS_LIST.size());
		if(PlayerConstants.SONGS_LIST.size() <= 0)
		PlayerConstants.SONGS_LIST = UtilFunctions.listOfDownloadedSongs(getApplicationContext());

		if(PlayerConstants.SONGS_LIST.size() <= 0){
			setContentView(R.layout.search_layout);
		//	btnSearch=(Button)findViewById(R.id.search_button);
		}
		else
			setListItems();
    }
public void searchVideos(View view) throws JSONException {
		TextView query= (TextView)findViewById(R.id.search);
		String text=query.getText().toString();
		setContentView(R.layout.activity_main);
		getViews();
		SendRequest(text);



}
	private void setListItems() {

		customAdapter = new CustomAdapter(this,R.layout.custom_list, PlayerConstants.SONGS_LIST);
		mediaListView.setAdapter(customAdapter); 
		mediaListView.setFastScrollEnabled(true);
	}
	
	private void getViews() {
		searchView=(SearchView)findViewById(R.id.searchView);
		searchView.setQueryHint("Search Videos");
		btnDownload=(Button)findViewById(R.id.btnMusicPlayer);
		materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
		floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
		floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
		floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
progressBarHolder =(FrameLayout)findViewById(R.id.progressBarHolder) ;
		playingSong = (TextView) findViewById(R.id.textNowPlaying);
		btnPlayer = (Button) findViewById(R.id.btnMusicPlayer);
		mediaListView = (ListView) findViewById(R.id.listViewMusic);
	//	mediaLayout = (LinearLayout) findViewById(R.id.linearLayoutMusicList);
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

		materialDesignFAM.setClosedOnTouchOutside(true);

		floatingActionButton1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PlayerConstants.AUTOREPLAY= !PlayerConstants.AUTOREPLAY;
				SongService.getMp().setLooping(PlayerConstants.AUTOREPLAY);
				if(PlayerConstants.AUTOREPLAY)

				{PlayerConstants.AUTONEXT= false;
				floatingActionButton1.setImageResource(R.drawable.baseline_toggle_on_black_18dp);
					floatingActionButton2.setImageResource(R.drawable.baseline_toggle_off_black_18dp);
				}
				else
				floatingActionButton1.setImageResource(R.drawable.baseline_toggle_off_black_18dp);

			}
		});
		floatingActionButton2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PlayerConstants.AUTONEXT= !PlayerConstants.AUTONEXT;
				if(PlayerConstants.AUTONEXT)
				{PlayerConstants.AUTOREPLAY= false;
					SongService.getMp().setLooping(PlayerConstants.AUTOREPLAY);

					floatingActionButton2.setImageResource(R.drawable.baseline_toggle_on_black_18dp);
					floatingActionButton1.setImageResource(R.drawable.baseline_toggle_off_black_18dp);
				}
				else
				floatingActionButton2.setImageResource(R.drawable.baseline_toggle_off_black_18dp);


			}
		});
		floatingActionButton3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {


					PlayerConstants.SONGS_LIST = UtilFunctions.listOfDownloadedSongs(getApplicationContext());

			setListItems();
			materialDesignFAM.close(true);
			}
		});
		btnDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doDownload(v);
			}
		});
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
	public static void showLoading(Boolean show){
		outAnimation = new AlphaAnimation(1f, 0f);
		inAnimation = new AlphaAnimation(0f, 1f);

		progressBarHolder.setAnimation(outAnimation);
		if(show)
		{ inAnimation.setDuration(200);
		progressBarHolder.setAnimation(inAnimation);
			progressBarHolder.setVisibility(View.VISIBLE);}
		else
		{outAnimation.setDuration(200);
		progressBarHolder.setAnimation(outAnimation);
			progressBarHolder.setVisibility(View.GONE);}
	}
	public static void changeUI(){
		updateUI();
		changeButton();
	}
	private  void  SendRequest(String query){
		RequestQueue queue = Volley.newRequestQueue(this);
		String url ="https://www.googleapis.com/youtube/v3/search?part=snippet&q="+query+"&type=video&order="+criteria+"&duration="+vidLen+"&maxResults="+maxres+"&key="+key;

// Request a string response from the provided URL.
		JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject  response)  {
						showLoading(false);
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


						}
						catch (Exception e) {
							e.printStackTrace();

						}
					}}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {showLoading(false);
				Log.d("That didn't work!","vbds");
			}
		});
// Add the request to the RequestQueue.
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
				MY_SOCKET_TIMEOUT_MS,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
				if(isInternetAvailable())
				{showLoading(true);queue.add(stringRequest);}
	else showAlert();

	}
	public void Download(){
		DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);

		DownloadManager.Request request= new DownloadManager.Request(Uri.parse(SongService.songPath));
		try{                request.setTitle(SongService.title + ".mp3");            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Yoump3/"  + SongService.title + ".mp3");

			dm.enqueue(request);}
		catch (Exception e){
			Log.e("exp",e.getMessage());}
	}
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
			//you have the permission now.
			Download();
		}
	}
	public  boolean haveStoragePermission() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED) {
				Log.e("Permission error","You have permission");Download();
				return true;
			} else {

				Log.e("Permission error","You have asked for permission");
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
				return false;
			}
		}
		else { //you dont need to worry about these stuff below api level 23
			Log.e("Permission error","You already have the permission");Download();
			return true;
		}
	}
	public  void  doDownload(View v){
		// String fileName=title.substring(0,10)+".mp3";
		//    downloadFile(songId,fileName);
		//  String[] args={songId,fileName};
		haveStoragePermission();
		// new DownloadFileFromURL().execute(args);

	}
   public  boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    public  void showAlert(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("No Internet")
                .setMessage("Please check your intenet connection and try again")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })


                .show();
    }
    private void ActionBarTitleGravity() {
        // TODO Auto-generated method stub

		ActionBar actionbar = getSupportActionBar();

		TextView textview = new TextView(getApplicationContext());

		RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        textview.setLayoutParams(layoutparams);

        textview.setText("YOUMP3");

        textview.setTextColor(Color.RED);

        textview.setGravity(Gravity.CENTER);

        textview.setTextSize(20);

        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        actionbar.setCustomView(textview);

    }
}