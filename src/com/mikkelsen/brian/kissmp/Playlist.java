package com.mikkelsen.brian.kissmp;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Playlist {
	
	private Song mNowPlaying;
	private Stack<Song> current = new Stack<Song>();
	private Map<Long, Song> history = new HashMap<Long,Song>();
	private Map<Song,Integer> savedState = null;
	private Library mLibrary = Library.getInstance();
		
	public void saveSongState(Song song, Integer position){
		// only allow one saved song
		if(savedState != null) return;

		savedState = new HashMap<Song, Integer>();
		savedState.put(song, position);
	}
	
	public Map<Song,Integer> getSaved(){
		if(savedState == null)	
			return null;	
		Map<Song,Integer> m = new HashMap<Song, Integer>(savedState);
		mNowPlaying = m.keySet().iterator().next();
		savedState = null;
		return m;
	}	
	
	public void push(Song song){
		current.push(song);
	}
	
	public Song next(){
		if (current.size() > 0)
			mNowPlaying = current.pop();
		else
			mNowPlaying = mLibrary.getRandom();
		
		history.put(System.currentTimeMillis(), mNowPlaying);
		return mNowPlaying;
	}
	
	public  Song nowPlaying(){
		return mNowPlaying;
	}
	
	private static Playlist mInstance = null;
	private Playlist(){ }	
	public static Playlist getInstance(){
		if (mInstance == null)
			mInstance = new Playlist();
		return mInstance; 
	}
}
