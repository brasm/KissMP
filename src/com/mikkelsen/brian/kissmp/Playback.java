package com.mikkelsen.brian.kissmp;

import java.io.File;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Playback extends Service implements OnCompletionListener{
	protected static final String TAG = "Playback";
	private final MediaPlayer mPlayer = new MediaPlayer();
	private Playlist playlist = Playlist.getInstance();
	private Song mNowPlaying = null;
	
	private boolean mPaused = false;
	private boolean messageInterruptDisabled = false;
		
	public void onCreate(){
		super.onCreate();
		mPlayer.setOnCompletionListener(this);		
		kingButtonListener();
	}

	//
	// Playback controls ------------------------------------------------------
	//
	
	/**
	 * Play next song from playlist.
	 * If a song has been interrupted, it is completed instead.
	 */
	void next() {
		messageInterruptDisabled = false;
		
		// Check if we should resume from an interrupted song
		// or get next song from playlist.
		int seekTo;
		Map<Song, Integer> map;
		if ((map = playlist.getSaved()) != null){
			mNowPlaying = map.keySet().iterator().next();
			seekTo = map.values().iterator().next();
			Log.d(TAG,"next(), resume interrupted song");
		} else {
			mNowPlaying = playlist.next();
			seekTo = 0;
		}
		
		try {
			mPlayer.reset();
			mPlayer.setDataSource(mNowPlaying.Path);
			mPlayer.prepare();
			mPlayer.seekTo(seekTo);
			mPlayer.start();
			mPaused = false;
			
			String np = mNowPlaying.Artist + " - " + mNowPlaying.Title;
			Toast.makeText(getApplicationContext(), np, Toast.LENGTH_SHORT).show();;
		} catch (Exception e){
			Log.d(TAG,"next() exception, " + e.getMessage());
			//next();
		}
	}
	
	/**
	 * Toggle playback, play or pause
	 */
	void toggle(){
		if(mPlayer.isPlaying()) {
			mPlayer.pause();
			mPaused = true;
			return;
		}
		
		// -----
		if(mPaused) {
			mPlayer.start();
			mPaused = false;
			return;
		}
		
		// -----
		next();
		mPaused = false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		next();
		
	}

	/**
	 * Interrupts current playback to play a message.
	 * After completion, the current song is resumed.
	 *
	 * @param path, path to message audio
	 */
	private void play_message(Song song){
		if(messageInterruptDisabled) 
			return;
		
		int position = mPlayer.getCurrentPosition();
		Song save = mNowPlaying;
		playlist.push(song);
		next();
		playlist.saveSongState(save, position);
		
		// call next() to reenable interrupts
		messageInterruptDisabled = true; 
	}


	//
	// King diamond button, power reciever ------------------------------------
	//
	private BroadcastReceiver mPowerReciever;
	private void kingButtonListener() { 
  	  	mPowerReciever = new BroadcastReceiver() {
  	  		Song king= new Song(new File("/sdcard/Music/King Diamond/Them/02 - Welcome Home.ogg"));
  	  		
   	  		@Override
  			public void onReceive(Context context, Intent intent) {
   	  			Log.d(TAG,"Activate The King");
   	  			Toast.makeText(getApplicationContext(), "KING", Toast.LENGTH_SHORT).show();
  	  			play_message(king);
  	  		}
  		};
  		
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        getApplicationContext().registerReceiver(mPowerReciever, filter);
  	}

	//
	// Bind Service -----------------------------------------------------------
	private PlaybackBinder mBinder = new PlaybackBinder();
	public class PlaybackBinder extends Binder {
		public Playback getService(){
			return Playback.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent i) {
		return mBinder;
	}
	
	//
	// Bound Service Methods --------------------------------------------------
	public int getProgress(){
		int progress;
		try{
			progress = 100 * mPlayer.getCurrentPosition() / mPlayer.getDuration();
		} catch (Exception e){
			progress = 0;
		}
		return progress;
	}
	
	public Song nowPlaying(){
		return mNowPlaying;
	}
		
	
}
	

	
