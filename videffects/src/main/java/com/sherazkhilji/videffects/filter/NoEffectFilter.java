package com.sherazkhilji.videffects.filter;

import android.os.Parcel;
import android.os.Parcelable;

import com.sherazkhilji.videffects.Constants;
import com.sherazkhilji.videffects.interfaces.Filter;

public class NoEffectFilter implements Filter {

    public NoEffectFilter() {

    }

    public NoEffectFilter(Parcel in) {

    }

    public static final Creator<NoEffectFilter> CREATOR = new Creator<NoEffectFilter>() {
        @Override
        public NoEffectFilter createFromParcel(Parcel in) {
            return new NoEffectFilter(in);
        }

        @Override
        public NoEffectFilter[] newArray(int size) {
            return new NoEffectFilter[size];
        }
    };

    @Override
    public String getVertexShader() {
        return Constants.DEFAULT_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n" + "void main() {\n"
                + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
                + "}\n";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}