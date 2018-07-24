package com.soufienov.yoump3;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static com.soufienov.yoump3.AudioPlayerActivity.context;

/**
 * Created by user on 17/07/2018.
 */

public class AudioProxy extends AsyncTask<String,Void,Void> {

    File myCacheFile;
public static void writeFile(String url) throws IOException {
    URL website = new URL(url);
    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
    FileOutputStream fos = new FileOutputStream("audio.mp3");
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    fos.close();
}



    @Override
    protected Void doInBackground(String... strings) {
        try {
            myCacheFile=  File.createTempFile("audio.mp3", null, context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        myCacheFile.setWritable(true,false);

        Log.e("filename",myCacheFile.getName());
        Log.e("filename",myCacheFile.getPath());

        URL website = null;
        try {
            website = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(myCacheFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
