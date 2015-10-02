package com.sherazkhilji.videffect.sample;

import com.sheraz.kira.videoeffect.R;
import com.sherazkhilji.videffect.BlackAndWhiteEffect;
import com.sherazkhilji.videffect.DocumentaryEffect;
import com.sherazkhilji.videffect.DuotoneEffect;
import com.sherazkhilji.videffect.InvertColorsEffect;
import com.sherazkhilji.videffect.view.VideoSurfaceView;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

public class SamplePlayerActivity extends Activity {

	private static final String TAG = "MediaPlayerSurfaceStubActivity";

	protected Resources mResources;

	private VideoSurfaceView mVideoView = null;
	private MediaPlayer mMediaPlayer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResources = getResources();
		mMediaPlayer = new MediaPlayer();

		try {
			// Load video file from SD Card
			// File dir = Environment
			// .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			// File file = new File(dir,
			// "sample.mp4");
			// mMediaPlayer.setDataSource(file.getAbsolutePath());
			// -----------------------------------------------------------------------
			// Load video file from Assets directory
			AssetFileDescriptor afd = getAssets().openFd("sample.mp4");
			mMediaPlayer.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		// Initialize VideoSurfaceView using code
		// mVideoView = new VideoSurfaceView(this);
		// setContentView(mVideoView);
		// or
		setContentView(R.layout.activity_sampleplayer);
		mVideoView = (VideoSurfaceView) findViewById(R.id.mVideoSurfaceView);
		mVideoView.init(mMediaPlayer,
				new DuotoneEffect(Color.YELLOW, Color.RED));

	}

	@Override
	protected void onResume() {
		super.onResume();
		mVideoView.onResume();
	}
}
