package com.soufienov.yoump3.util;

import java.util.ArrayList;
import android.os.Handler;

public class PlayerConstants {
	//List of Songs
	public static ArrayList<MediaItem> SONGS_LIST = new ArrayList<MediaItem>();
	//song number which is playing right now from SONGS_LIST
	public static int SONG_NUMBER = 0;
	//song is playing or paused
	public static boolean SONG_PAUSED = true;
	public static  boolean BROWSER_OPENED= false;
	public static int TRIES=0;
	public static String MAXRES="30";

	public static boolean AUTOREPLAY=false;
	public static boolean AUTONEXT=false;

	//song changed (next, previous)
	public static boolean SONG_CHANGED = false;
	//handler for song changed(next, previous) defined in service(SongService)
	public static Handler SONG_CHANGE_HANDLER;
	//handler for song play/pause defined in service(SongService)
	public static Handler PLAY_PAUSE_HANDLER;
	//handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
	public static Handler PROGRESSBAR_HANDLER;
}
