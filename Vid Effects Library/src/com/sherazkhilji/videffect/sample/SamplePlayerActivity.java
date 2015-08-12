package com.sherazkhilji.videffect.sample;

import java.io.File;

import com.sherazkhilji.videffect.view.VideoSurfaceView;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
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

		mVideoView = new VideoSurfaceView(this);
		mVideoView.init(mMediaPlayer);
		setContentView(mVideoView);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mVideoView.onResume();
	}
}
