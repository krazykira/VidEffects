package com.videffects.sample.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AssetsGalleryModel {

    private Context ctx;

    public AssetsGalleryModel(Context ctx) {
        this.ctx = ctx;
    }

    private HashMap<String, Drawable> thumbnails = new HashMap<>();

    private ColorStateList color = ColorStateList.valueOf(Color.GRAY);

    private String[] assets = {
            "sample.mp4",
            "video_0.mp4",
            "video_2.mp4"
    };

    public String getAssetName(int position) {
        return assets[position];
    }

    public Drawable getThumbnail(int position) {
        String asset = assets[position];
        if (!thumbnails.containsKey(asset)) {
            cacheThumbnail(asset);
        }
        return thumbnails.get(asset);
    }

    private void cacheThumbnail(String asset) {
        try {
            InputStream ims = ctx.getAssets().open(asset.replace("mp4", "png"));
            Drawable thumbnail = Drawable.createFromStream(ims, null);
            thumbnails.put(asset, new RippleDrawable(color, thumbnail, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return assets.length;
    }
}
