package com.soufienov.yoump3.util;

import org.json.JSONException;
import org.json.JSONObject;

public class MediaItem {
	String title;
	String artist;
	String album;
	String path;

	public String getThumbnail() {
		return thumbnail;
	}

	String thumbnail;
	long duration;
	long albumId;
	String composer;

	public MediaItem() {
	}

	public MediaItem(JSONObject item) throws JSONException {

		this.artist = item.getJSONObject("snippet").getString("description");
		this.path = item.getJSONObject("id").getString("videoId");

		this.thumbnail = item.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("url");
		this.title = item.getJSONObject("snippet").getString("title");
	}

	@Override
	public String toString() {
		return title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		if(artist!=null)return artist;
		else return "";
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		if(album!=null)return album;
		else  return "";
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getPath() {
if(path!=null)
		return path;
else return "";
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getComposer() {
		if(composer!=null)return composer;
		else return "";
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

}