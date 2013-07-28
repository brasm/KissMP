package com.mikkelsen.brian.kissmp;

import java.util.List;

import com.mikkelsen.brian.kissmp.Playback.PlaybackBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class KissMPActivity extends Activity {
	private static final String TAG = "KissMP";
	private Handler mHandler = new Handler();
	private Thread updateUI;
	private Runnable drawScreenRunner;
	private Intent mServiceIntent;
	
	Playlist mPlaylist = Playlist.getInstance();
	Library mLibrary = Library.getInstance();
	
	//  Activity States 
	// ----------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);
	    initializeUI();
	    mServiceIntent = new Intent(this, Playback.class);
		startService(mServiceIntent);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
	}

	public void onResume(){
		Log.d(TAG, "onResume()");
		drawScreenRunner = new Runnable(){
			@Override
			public void run() {
				drawScreen();
				mHandler.postDelayed(drawScreenRunner, 1000);
			}
		};
		super.onResume();
		updateUI = new Thread(drawScreenRunner);
		updateUI.start();
	}
	
	public void onPause(){
		super.onPause();
		updateUI.stop();
	}
	
	public void onStop(){
		super.onStop();
		if(mBound) unbindService(mConnection);
	}
	
	public void onDestroy(){
		super.onDestroy();
		stopService(mServiceIntent);
	}
	
    //   Playback Service 
    // ----------------------------------------------------------------------
	private Playback mPlayer;
	boolean mBound = false;
	
	ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlaybackBinder binder = (PlaybackBinder) service;
            mPlayer = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    //   User Interface
    // ----------------------------------------------------------------------

	
	
	private OnClickListener clickListeners = new OnClickListener(){	
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.btnNext:	mPlayer.next(); break;
			case R.id.btnToggle: mPlayer.toggle(); break;
			}
		}
	};
	
	private ProgressBar mProgress;
	private TextView mNpArtist;
	private Button mToggleBtn;
	private Button mNextBtn;
	private TextView mNpSong;
	private TextView mLibSize;
	private ListView mLibraryBrowser;
	
	
	private void initializeUI(){
        setContentView(R.layout.simple);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mNpArtist = (TextView) findViewById(R.id.np_artist);
		mNpSong = (TextView) findViewById(R.id.np_song);
		mLibSize = (TextView) findViewById(R.id.librarySize);
		
		mToggleBtn = (Button) findViewById(R.id.btnToggle);		
		mNextBtn = (Button) findViewById(R.id.btnNext);
		
		mLibraryBrowser = (ListView) findViewById(R.id.libBrowser);

		mLibraryBrowser.setAdapter(new SimpleAdapter(this, data, resource, from, to));
		
		
		mToggleBtn.setOnClickListener(clickListeners);
		mNextBtn.setOnClickListener(clickListeners);
	}
	
	private void drawScreen(){
		try {
			Song s = mPlaylist.nowPlaying();
			mNpArtist.setText(s.Artist);
			mNpSong.setText(s.Title);
			mProgress.setProgress(mPlayer.getProgress());
			mLibSize.setText(mLibrary.size().toString() + " Songs in Library");
		} catch(Exception e){}
	}
}
