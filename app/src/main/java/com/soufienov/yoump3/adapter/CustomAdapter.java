package com.soufienov.yoump3.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.soufienov.yoump3.R;
import com.soufienov.yoump3.util.MediaItem;
import com.soufienov.yoump3.util.UtilFunctions;
import com.squareup.picasso.Picasso;

public class CustomAdapter extends ArrayAdapter<MediaItem>{

	ArrayList<MediaItem> listOfSongs;
	Context context;
	LayoutInflater inflator;
	
	public CustomAdapter(Context context, int resource,	ArrayList<MediaItem> listOfSongs) {
		super(context, resource, listOfSongs);
		this.listOfSongs = listOfSongs;
		this.context = context;
		inflator = LayoutInflater.from(context);
	}

	private class ViewHolder{
		TextView textViewSongName, textViewArtist, textViewDuration;
		ImageView thumbnail;
	}
	
	ViewHolder holder;
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View myView = convertView;
		if(convertView == null){
			myView = inflator.inflate(R.layout.custom_list, parent, false);
			holder = new ViewHolder();
			holder.textViewSongName = (TextView) myView.findViewById(R.id.textViewSongName);
			holder.textViewArtist = (TextView) myView.findViewById(R.id.textViewArtist);
			holder.textViewDuration = (TextView) myView.findViewById(R.id.textViewDuration);
			holder.thumbnail=(ImageView)myView.findViewById(R.id.thumb);
			myView.setTag(holder);
		}else{
			holder = (ViewHolder)myView.getTag();
		}
		MediaItem detail = listOfSongs.get(position);
		holder.textViewSongName.setText(detail.toString());
		holder.textViewArtist.setText(detail.getAlbum() + " - " + detail.getArtist());
		if(detail.getThumbnail()!=null)
		Picasso.get().load(detail.getThumbnail()).into(holder.thumbnail);
		else
		{	Bitmap albumArt = UtilFunctions.getAlbumart(context, detail.getAlbumId());
		if(albumArt != null){
			holder.thumbnail.setBackgroundDrawable(new BitmapDrawable(albumArt));
		}else{
			holder.thumbnail.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
		}}
if(detail.getDuration()>0)
		holder.textViewDuration.setText(UtilFunctions.getDuration(detail.getDuration()));
		return myView;
	}
}
