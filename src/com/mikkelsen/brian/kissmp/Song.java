package com.mikkelsen.brian.kissmp;

import java.io.File;
import android.media.MediaMetadataRetriever;
import android.util.Log;

class Song{ 

	private static final String TAG = "Song";
		
	public  String Artist;
	public String Album;
	public String Title;
	public String Path;
	public Integer Track = -1;
	public Integer Duration = -1;
	
	public Song(File file){
		MediaMetadataRetriever m = new MediaMetadataRetriever();

		try {
			m.setDataSource(file.getAbsolutePath());
			Path = file.getAbsolutePath();
			Artist = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			Album = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
			Title = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			String _track = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
			String _duration = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			Track = Integer.parseInt(_track);
			Duration = Integer.parseInt(_duration);
			
		} catch (Exception e) {
			Title = file.getName();
			Path = file.getAbsolutePath();
			Album = "Unknown";
			Artist = "Unknown";
			Track = 0;
			Duration = 0;
			Log.d(TAG," Exception in Song: " + e.getMessage());
		}
	}
}

