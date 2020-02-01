package com.sherazkhilji.videffects.filter;

import android.os.Parcel;
import android.os.Parcelable;

import com.sherazkhilji.videffects.Constants;
import com.sherazkhilji.videffects.interfaces.Filter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GrainFilter implements Filter {

    private String scaleString = "scale = 1.0 ;\n";
    private String stepX, stepY;
    private String[] seedString = new String[2];

    public GrainFilter(int width, int height) {
        this.stepX = "stepX = " + 0.5f / width + ";\n";
        this.stepY = "stepY = " + 0.5f / height + ";\n";
    }

    public GrainFilter(Parcel in) {
        scaleString = in.readString();
        stepX = in.readString();
        stepY = in.readString();
        seedString = in.createStringArray();
    }

    public static final Creator<GrainFilter> CREATOR = new Creator<GrainFilter>() {
        @Override
        public GrainFilter createFromParcel(Parcel in) {
            return new GrainFilter(in);
        }

        @Override
        public GrainFilter[] newArray(int size) {
            return new GrainFilter[size];
        }
    };

    public void setStrength(float strength) {
        scaleString = "scale = " + strength + ";\n";
    }

    @Override
    public String getVertexShader() {
        return Constants.DEFAULT_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        seedString[0] = "seed[0] = " + ThreadLocalRandom.current().nextFloat() + ";\n";
        seedString[1] = "seed[1] = " + ThreadLocalRandom.current().nextFloat() + ";\n";
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + " vec2 seed;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES tex_sampler_0;\n"
                + "uniform samplerExternalOES tex_sampler_1;\n"
                + "float scale;\n"
                + " float stepX;\n"
                + " float stepY;\n"
                + "float rand(vec2 loc) {\n"
                + "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n"
                + "  float theta2 = dot(loc, vec2(12.0, 78.0));\n"
                + "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n"
                + "  float temp = mod(197.0 * value, 1.0) + value;\n"
                + "  float part1 = mod(220.0 * temp, 1.0) + temp;\n"
                + "  float part2 = value * 0.5453;\n"
                + "  float part3 = cos(theta1 + theta2) * 0.43758;\n"
                + "  float sum = (part1 + part2 + part3);\n"
                + "  return fract(sum)*scale;\n"
                + "}\n"
                + "void main() {\n"
                + seedString[0]
                + seedString[1]
                + scaleString
                + stepX
                + stepY
                + "  float noise = texture2D(tex_sampler_1, vTextureCoord + vec2(-stepX, -stepY)).r * 0.224;\n"
                + "  noise += texture2D(tex_sampler_1, vTextureCoord + vec2(-stepX, stepY)).r * 0.224;\n"
                + "  noise += texture2D(tex_sampler_1, vTextureCoord + vec2(stepX, -stepY)).r * 0.224;\n"
                + "  noise += texture2D(tex_sampler_1, vTextureCoord + vec2(stepX, stepY)).r * 0.224;\n"
                + "  noise += 0.4448;\n"
                + "  noise *= scale;\n"
                + "  vec4 color = texture2D(tex_sampler_0, vTextureCoord);\n"
                + "  float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;\n"
                + "  float mask = (1.0 - sqrt(energy));\n"
                + "  float weight = 1.0 - 1.333 * mask * noise;\n"
                + "  gl_FragColor = vec4(color.rgb * weight, color.a);\n"
                + "  gl_FragColor = gl_FragColor+vec4(rand(vTextureCoord + seed), rand(vTextureCoord + seed),rand(vTextureCoord + seed),1);\n"
                + "}\n";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(scaleString);
        dest.writeString(stepX);
        dest.writeString(stepY);
        dest.writeStringArray(seedString);
    }
}
