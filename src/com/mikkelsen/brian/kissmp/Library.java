package com.mikkelsen.brian.kissmp;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import android.util.Log;

public class Library {
	private static final String TAG = "Library";
	
	private volatile Map<String,Map<String,List<String>>> _library;	
	private volatile List<String> _artists;
	private volatile List<String> _albums;
	private volatile List<Song> _songs;
	private volatile boolean _scanning;
	private Random _rand;
	
	private static Library mInstance = null;
	
	public static Library getInstance() {
		if (mInstance == null) {
			mInstance = new Library();
		}
		return mInstance;
	}
	
	private Library() {
		_library = new HashMap<String, Map<String,List<String>>>();
		_artists = new Vector<String>();
		_albums = new Vector<String>();
		_songs = new Vector<Song>();
		_scanning = false;		
		_rand = new Random();
		scan_library();
	}
	
	public synchronized void scan_library(){
		if(_scanning) return;
		_scanning = true;
		new Thread(scanFolder).start();
	}
	
	public Song getRandom(){
		Song random;
		try {
			int max = _songs.size() - 1;
			int idx = _rand.nextInt(max);
			random = _songs.get(idx);
			if(random == null) 
				throw new NullPointerException();
			
		} catch (Exception e) {
			random = new Song(new File("/sdcard/Music/King Diamond/04 - Tea.ogg"));
			Log.d("TAG", "Ecxeption in getRandom, " + e.getMessage());
		}	
		return random;
	}
	
	private void addSong(File f){
		Song s = new Song(f);
		addSong(s);
	}
		
	private void addSong(Song s){
		Map<String,Map<String, List<String>>> _byartist = _library;
		Map<String, List<String>> _byalbum;
		List<String> _bysong;
		
		if (! _byartist.containsKey(s.Artist)){
			_byartist.put(s.Artist, new HashMap<String, List<String>>());
			_artists.add(s.Artist);
			Log.d(TAG, "New Artist, "+s.Artist);
		}

		_byalbum = _byartist.get(s.Artist);
		if (! _byalbum.containsKey(s.Album)){
			_byalbum.put(s.Album, new Vector<String>());
			_albums.add(s.Album);
			Log.d(TAG, "New Album, "+s.Album);

		}
		
		_bysong = _byalbum.get(s.Album);
		if( ! _bysong.contains(s.Path)){
			_bysong.add(s.Path);
			_songs.add(s);
			Log.d(TAG, "Adding Song, "+s.Path);
		}
	}
		
	private Runnable scanFolder = new Runnable() {
		@Override
		public void run() {
			File sdcard = new File("/sdcard/");
			try{
				//readCache();
				scanFilesystem(sdcard);
				//writeCache();
			} catch (Exception e){
				Log.d(TAG, "Exception in scanFolder, " + e.getMessage());
			}
			finally {
				_scanning = false;
			}
			Log.d(TAG, "Scan complete");
			
			
		}
	};
	
	private void scanFilesystem(File dir) {
		Log.d(TAG, "scanFilesystem(File) " + dir.getName());
		if(! isDirectory(dir)) return;
		for (File f : dir.listFiles()){
			if(isMusicFile(f))	addSong(f);
		}
		// scan sub directories
		for (File subdir : dir.listFiles())
			if (isDirectory(subdir)) scanFilesystem(subdir);
	}

	private boolean isDirectory(File file){
		return file.isDirectory() && file.canRead();
	}

	
	private boolean isMusicFile(File file){	
		String fileTypeRegex = ".*\\.(flac|ogg|oga|mp3|wma|m4a)";
		String filename = file.getName().toLowerCase();
		return file.isFile() && file.canRead() && filename.matches(fileTypeRegex);
	}

	public Integer size() {
		return _songs.size();
	}
}