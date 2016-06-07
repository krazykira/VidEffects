# VidEffects
This is an Android library which can be used to apply different Filters/Effects on videos. It uses vertexShaders and fragmentShaders to apply effects on `GLSurfaceView`. It uses `MediaPlayer` instance for playing videos on `GlSurfaceView`. See the sample app in order to see a working demo.

## Supported Effects

The following list of effects are currently avaialble and can be applied using VidEffects
* [AutoFix Effect] (https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/AutoFixEffect.java)
* [Black and White Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/BlackAndWhiteEffect.java)
* [Brightness Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/BrightnessEffect.java)
* [Contrast Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/ContrastEffect.java)
* [CrossProcess Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/CrossProcessEffect.java)
* [Documentary Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/DocumentaryEffect.java)
* [Duotone Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/DuotoneEffect.java)
* [FillLight Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/FillLightEffect.java)
* [Grain Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/GrainEffect.java)
* [Greyscale Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/GreyScaleEffect.java)
* [Lamoish Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/LamoishEffect.java)
* [InvertColors Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/InvertColorsEffect.java)
* [Posterize Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/PosterizeEffect.java)
* [Saturation Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/SaturationEffect.java)
* [Sepia Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/SepiaEffect.java)
* [Sharpness Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/SharpnessEffect.java)
* [Temperature Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/TemperatureEffect.java)
* [Tint Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/TintEffect.java)
* [Vignette Effect](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/VignetteEffect.java)


## Limitations

The effects applied using this library are temporary. What that means is that the orignal video doesn't change. Effects are only applied during video playback and once the video ends the effects end with it. In the future, i am aiming to apply permanant effect to videos. You guys are welcome to help out using PRs. 
For now, if you are really desperate want to apply effects then you can use [FFmpeg](https://ffmpeg.org/) to apply effects on videos. Details about that can be seen on this [wiki page](https://github.com/krazykira/VidEffects/wiki/Permanent-video-effects)

## How to use it

- Download and Add VidEffects as Library project
- You can add the `VideoSurfaceView` using either `java code` or as `xml` in your `layout` file.
```sh
<com.sherazkhilji.videffect.view.VideoSurfaceView
        android:id="@+id/mVideoSurfaceView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
- Then you need to reference the `VideoSurfaceView` and call its `init()` method in your `Activity` or `Fragment` `onCreate()` supplying it with a `MediaPlayer` instance and a `Video Effect`. Also you would need to call `VideoSurfaceView` `onResume()` in your `Activity` or `Fragment`  `onResume()` so that your video is rendered properly.

 ```sh
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
	
		mVideoView = (VideoSurfaceView) findViewById(R.id.mVideoSurfaceView);
		mVideoView.init(mMediaPlayer,
				new DuotoneEffect(Color.YELLOW, Color.RED));
	        setContentView(R.layout.activity_sampleplayer);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mVideoView.onResume();
	}
 ```


- Incase of any confusion, checkout the [SamplePlayerActivity](https://github.com/krazykira/VidEffects/blob/master/Vid%20Effects%20Library/src/com/sherazkhilji/videffect/sample/SamplePlayerActivity.java) for a complete example on how to apply different `Effects` on your videos.

## See it in working

#### Video screenshot without any Effect
![Video screenshot without any Effect](https://cloud.githubusercontent.com/assets/2201511/9244232/ded8b760-41b2-11e5-9e4b-54d7c0b9cfca.png)

#### Video screenshot with Black and White Effect
![Video screenshot with Black and White Effect](https://cloud.githubusercontent.com/assets/2201511/9244235/e75ab7a8-41b2-11e5-90b7-33d944d1d6c8.png)

#### Video screenshot with Invert Colors Effect
![Video screenshot with Invert Colors Effect](https://cloud.githubusercontent.com/assets/2201511/9244236/ea09d344-41b2-11e5-9e71-f04601fd61e9.png)

## Special Thanks to

* This blog [post](http://code.tutsplus.com/tutorials/how-to-use-android-media-effects-with-opengl-es--cms-23650) by Ashraff Hathibelagal for easing the process of learning Opengl and explaining how to apply effects on images.
* [GrepCode](http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.0.1_r1/android/filterpacks/imageproc/package-info.java) For providing me with Android source code which were helpful in writing video effects/filters.
* [MediaPlayerSurface by crossle](https://github.com/crossle/MediaPlayerSurface) It helped me in playing video using a GlsurfaceView.
* [Fadden](http://stackoverflow.com/questions/31805837/applying-effects-on-video-being-played/31958741#comment51571387_31805837) who is the writer of [grafika](https://github.com/google/grafika) for pointing me towards right direction on how to apply effects on a video.
* [Intel INDE Media for Mobile Tutorials](https://software.intel.com/en-us/articles/intel-inde-media-pack-for-android-tutorials-building-samples) for the sample app and helping me getting familiar with how fragmentShaders work.
* [Umair Shafique](https://github.com/muhammad-umair-khan) for authoring the [How to apply permanent effect using FFmpeg](https://github.com/krazykira/VidEffects/wiki/Permanent-video-effects)  wiki page.
 

### Development

Want to contribute or add some new Effects? Great! Fork it and send me a pull request or contact me on the email below, if you want to become a permanant contributor.


Developed by
============

* Sheraz Ahmad Khilji - <sherazkhilji@gmail.com>


License
=======

    Copyright 2015 Sheraz Ahmad Khilji

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
