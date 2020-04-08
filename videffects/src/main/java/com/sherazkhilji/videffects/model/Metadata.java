package com.sherazkhilji.videffects.model;

public class Metadata {

    private double width;
    private double height;
    private int bitrate;

    public Metadata(double width, double height, int bitrate) {
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getBitrate() {
        return bitrate;
    }
}
