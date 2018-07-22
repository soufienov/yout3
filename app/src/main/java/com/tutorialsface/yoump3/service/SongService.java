package com.tutorialsface.yoump3.service;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.RemoteViews;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tutorialsface.yoump3.AudioPlayerActivity;
import com.tutorialsface.yoump3.AudioProxy;
import com.tutorialsface.yoump3.MainActivity;
import com.tutorialsface.yoump3.R;
import com.tutorialsface.yoump3.WebviewActivity;
import com.tutorialsface.yoump3.controls.Controls;
import com.tutorialsface.yoump3.receiver.NotificationBroadcast;
import com.tutorialsface.yoump3.util.MediaItem;
import com.tutorialsface.yoump3.util.PlayerConstants;
import com.tutorialsface.yoump3.util.UtilFunctions;

public class SongService extends Service implements AudioManager.OnAudioFocusChangeListener{
	String LOG_CLASS = "SongService";

	public static MediaPlayer getMp() {
		return mp;
	}

	private static MediaPlayer mp;
	int NOTIFICATION_ID = 1111;
	public static final String NOTIFY_PREVIOUS = "com.tutorialsface.yoump3.previous";
	public static final String NOTIFY_DELETE = "com.tutorialsface.yoump3.delete";

	public static void setMp(MediaPlayer mp) {
		SongService.mp = mp;
	}

	public static final String NOTIFY_PAUSE = "com.tutorialsface.yoump3.pause";
	public static final String NOTIFY_PLAY = "com.tutorialsface.yoump3.play";
	public static final String NOTIFY_NEXT = "com.tutorialsface.yoump3.next";
	private static final int MY_SOCKET_TIMEOUT_MS = 20000;
public static  String songPath="",title;
	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	AudioManager audioManager;
	Bitmap mDummyAlbumArt;
	private static Timer timer; 
	private static boolean currentVersionSupportBigNotification = false;
	private static boolean currentVersionSupportLockScreenControls = false;
	private File song;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
Log.e("looping",mp.isLooping()+"");
        currentVersionSupportBigNotification = UtilFunctions.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = UtilFunctions.currentVersionSupportLockScreenControls();
        timer = new Timer();
        mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(PlayerConstants.AUTONEXT)
				Controls.nextControl(getApplicationContext());
				mp.setLooping(PlayerConstants.AUTOREPLAY);
				Log.e("looping1111",mp.isLooping()+"");
			}
		});
		super.onCreate();

	}

	/**
	 * Send message from timer
	 * @author jonty.ankit
	 */
	private class MainTask extends TimerTask{ 
        public void run(){
            handler.sendEmptyMessage(0);
        }
    } 
	
	 private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
        	if(mp != null){
        		int progress = (mp.getCurrentPosition()*100) / mp.getDuration();
        		Integer i[] = new Integer[3];
        		i[0] = mp.getCurrentPosition();
        		i[1] = mp.getDuration();
        		i[2] = progress;
        		try{
        			PlayerConstants.PROGRESSBAR_HANDLER.sendMessage(PlayerConstants.PROGRESSBAR_HANDLER.obtainMessage(0, i));
        		}catch(Exception e){}
        	}
    	}
    }; 
	    
    @SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if(PlayerConstants.SONGS_LIST.size() <= 0){
				PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
			}
			MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
			if(currentVersionSupportLockScreenControls){
				RegisterRemoteClient();
			}
			getmp3(data,getApplicationContext());

			
			PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					PlayerConstants.BROWSER_OPENED=false;
					MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
					getmp3(data,getApplicationContext());
					/*newNotification();
					try{
						playSong(songPath, data);
						MainActivity.changeUI();
						AudioPlayerActivity.changeUI();
					}catch(Exception e){
						e.printStackTrace();
					}*/
					return false;
				}
			});
			
			PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					String message = (String)msg.obj;
					if(mp == null)
						return false;
					if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
						PlayerConstants.SONG_PAUSED = false;
						if(currentVersionSupportLockScreenControls){
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
						}
						mp.start();
					}else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
						PlayerConstants.SONG_PAUSED = true;
						if(currentVersionSupportLockScreenControls){
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
						}
						mp.pause();
					}
					newNotification();
					try{
						MainActivity.changeButton();
						AudioPlayerActivity.changeButton();
					}catch(Exception e){}
					Log.d("TAG", "TAG Pressed: " + message);
					return false;
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	private void getmp3(final MediaItem data, Context context) {
		title=data.getTitle();
		song=null;
		MainActivity.btnDownload.setVisibility(View.GONE);

		if(hitCache(title)!=null){
			songPath=song.getPath();
			playSong(songPath, data);

			newNotification();
			try{
				MainActivity.changeUI();
				AudioPlayerActivity.changeUI();}
			catch (Exception e){e.printStackTrace();}
			}
		else
		{
		String urli = "http://youtubeinmp3.me/api/generate.php?id=" + data.getPath();
		//	progressBar.setVisibility(View.VISIBLE);

		RequestQueue queue = Volley.newRequestQueue(context);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, urli,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						// Display the first 500 characters of the response string.
						Log.e("mp3 link",response);
						try {int st = response.indexOf("n('");
						String nwresp = response.substring(st + 3);
						int end = nwresp.indexOf("'");
						nwresp = nwresp.substring(0, end);
						 songPath = nwresp;
							Log.e("pre-xception ===>",songPath);

							playSong(songPath, data);

							newNotification();
							//path = nwresp;
						} catch (Exception e) {
							Log.e("execption ===>",songPath);

							e.printStackTrace();
							loadiframe(songPath);

						}
						try{
						MainActivity.changeUI();
						AudioPlayerActivity.changeUI();}
						catch (Exception e){e.printStackTrace();}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {                       //     progressBar.setVisibility(View.GONE);

				AlertDialog.Builder alertDialog=new AlertDialog.Builder(getApplicationContext());

				alertDialog.setTitle("Error");
				alertDialog.setMessage("Please check your internet and try again");
			alertDialog.show();
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

	private void loadiframe( String songpath) {
		Log.e("iframe",songpath);
    	if(PlayerConstants.BROWSER_OPENED==false)
		{	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(songpath));
		startActivity(browserIntent);
    	/*Intent intent= new Intent(this,WebviewActivity.class);
    	intent.putExtra("link","www.google.com");
    	startActivity(intent);*/
    	Log.e("error_reading","open ifreame");
		PlayerConstants.BROWSER_OPENED=true;
		}
	}
	/**
	 * Notification
	 * Custom Bignotification is available from API 16
	 */
	@SuppressLint("NewApi")
	private void newNotification() {
		String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getTitle();
		String albumName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);
		 
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.drawable.ic_music)
        .setContentTitle(songName).build();

		setListeners(simpleContentView);
		setListeners(expandedView);
		
		notification.contentView = simpleContentView;
		if(currentVersionSupportBigNotification){
			notification.bigContentView = expandedView;
		}
		
		try{
			long albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumId();
			Bitmap albumArt = UtilFunctions.getAlbumart(getApplicationContext(), albumId);
			if(albumArt != null){
				notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				if(currentVersionSupportBigNotification){
					notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				}
			}else{
				notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
				if(currentVersionSupportBigNotification){
					notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(PlayerConstants.SONG_PAUSED){
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		}else{
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}

		notification.contentView.setTextViewText(R.id.textSongName, songName);
		notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
		if(currentVersionSupportBigNotification){
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
			notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}
	
	/**
	 * Notification click listeners
	 * @param view
	 */
	public void setListeners(RemoteViews view) {
		Intent previous = new Intent(NOTIFY_PREVIOUS);
		Intent delete = new Intent(NOTIFY_DELETE);
		Intent pause = new Intent(NOTIFY_PAUSE);
		Intent next = new Intent(NOTIFY_NEXT);
		Intent play = new Intent(NOTIFY_PLAY);
		
		PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

		PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, pDelete);
		
		PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pPause);
		
		PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, pNext);
		
		PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

	}
	
	@Override
	public void onDestroy() {
		if(mp != null){
			mp.stop();
			mp = null;
		}
		super.onDestroy();
	}

	/**
	 * Play song, Update Lockscreen fields
	 * @param songPath
	 * @param data
	 */
	@SuppressLint("NewApi")
	private void playSong(String songPath, MediaItem data) {
		PlayerConstants.TRIES++;
		try {
			if(currentVersionSupportLockScreenControls){
				UpdateMetadata(data);
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
			mp.reset();

			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			timer.scheduleAtFixedRate(new MainTask(), 0, 100);
			if(!song.isFile())
			MainActivity.btnDownload.setVisibility(View.VISIBLE);

		} catch (IOException e) {
			//e.printStackTrace();
			Log.e("err","from here!!");
			if(PlayerConstants.TRIES<3)
			playSong(songPath,data);
			else loadiframe(songPath);
		}
		PlayerConstants.BROWSER_OPENED=true;
	}
	@SuppressLint("NewApi")
	private void RegisterRemoteClient(){
		remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
		 try {
		   if(remoteControlClient == null) {
			   audioManager.registerMediaButtonEventReceiver(remoteComponentName);
			   Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			   mediaButtonIntent.setComponent(remoteComponentName);
			   PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
			   remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			   audioManager.registerRemoteControlClient(remoteControlClient);
		   }
		   remoteControlClient.setTransportControlFlags(
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
				   RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_STOP |
				   RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
				   RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
	  }catch(Exception ex) {
	  }
	}
	
	@SuppressLint("NewApi")
	private void UpdateMetadata(MediaItem data){
		if (remoteControlClient == null)
			return;
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.getAlbum());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.getArtist());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.getTitle());
		mDummyAlbumArt = UtilFunctions.getAlbumart(getApplicationContext(), data.getAlbumId());
		if(mDummyAlbumArt == null){
			mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
		}
		metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
		metadataEditor.apply();
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {}
	
public File hitCache(String title){

 song=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Yoump3/"+title+".mp3");
if(song.isFile()){return song;}
else return null;
}
}