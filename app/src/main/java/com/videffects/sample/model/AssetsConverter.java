package com.videffects.sample.model;

import android.content.res.AssetFileDescriptor;

import com.sherazkhilji.videffects.model.Converter;
import com.sherazkhilji.videffects.model.Metadata;

import java.io.IOException;

public class AssetsConverter extends Converter {

    public AssetsConverter(AssetFileDescriptor assetFileDescriptor) {
        setMetadata(assetFileDescriptor);
        try {
            extractor.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMetadata(AssetFileDescriptor assetFileDescriptor) {
        Metadata metadata = new AssetsMetadataExtractor().extract(assetFileDescriptor);
        if (metadata != null) {
            width = (int) metadata.getWidth();
            height = (int) metadata.getHeight();
            bitrate = metadata.getBitrate();
        }
    }
}
