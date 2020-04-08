package com.videffects.sample.model;

import android.content.res.AssetFileDescriptor;

import com.sherazkhilji.videffects.model.Metadata;
import com.sherazkhilji.videffects.model.MetadataExtractor;

public class AssetsMetadataExtractor extends MetadataExtractor {

    public Metadata extract(AssetFileDescriptor assetFileDescriptor) {
        retriever.setDataSource(
                assetFileDescriptor.getFileDescriptor(),
                assetFileDescriptor.getStartOffset(),
                assetFileDescriptor.getLength()
        );
        return extractMetadata();
    }

}
