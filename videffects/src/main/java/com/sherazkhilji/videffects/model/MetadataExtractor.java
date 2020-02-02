package com.sherazkhilji.videffects.model;

import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.METADATA_KEY_BITRATE;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION;

public class MetadataExtractor {

    private MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private Metadata metadata;

    public Metadata extract(String path) {
        retriever.setDataSource(path);
        extractMetadata();
        return metadata;
    }

    public Metadata extract(AssetFileDescriptor assetFileDescriptor) {
        retriever.setDataSource(
                assetFileDescriptor.getFileDescriptor(),
                assetFileDescriptor.getStartOffset(),
                assetFileDescriptor.getLength()
        );
        extractMetadata();
        return metadata;
    }

    private void extractMetadata() {
        String rotationString = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION);
        String bitrateString = retriever.extractMetadata(METADATA_KEY_BITRATE);
        String widthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String heightString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

        try {
            int rotation = Integer.parseInt(rotationString);
            int bitrate = Integer.parseInt(bitrateString);
            double width = Double.parseDouble(widthString);
            double height = Double.parseDouble(heightString);

            if (rotation == 90 || rotation == 270) {
                metadata = new Metadata(height, width, bitrate);
            } else {
                metadata = new Metadata(width, height, bitrate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
    }
}
