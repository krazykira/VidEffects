package com.sherazkhilji.videffects.filter;

import com.sherazkhilji.videffects.Constants;
import com.sherazkhilji.videffects.interfaces.Filter;

public class AutoFixFilter implements Filter {

    private static final float SHIFT_SCALE = 1.0f / 256f;
    private static final float HIST_OFFSET = 0.5f / 766f;
    private static final float HIST_SCALE = 765f / 766f;
    private static final float DENSITY_OFFSET = 0.5f / 1024f;
    private static final float DENSITY_SCALE = 1023f / 1024f;

    private float intensity = 0.0F;
    private String shader;

    @Override
    public void setIntensity(float strength) {
        this.intensity = strength;
    }

    @Override
    public String getVertexShader() {
        return Constants.DEFAULT_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES tex_sampler_0;\n"
                + "uniform samplerExternalOES tex_sampler_1;\n"
                + "uniform samplerExternalOES tex_sampler_2;\n"
                + " float scale;\n" + " float shift_scale;\n"
                + " float hist_offset;\n" + " float hist_scale;\n"
                + " float density_offset;\n" + " float density_scale;\n"
                + "varying vec2 vTextureCoord;\n" + "void main() {\n"
                + "  shift_scale = " + SHIFT_SCALE + ";\n"
                + "  hist_offset = " + HIST_OFFSET + ";\n"
                + "  hist_scale = " + HIST_SCALE + ";\n"
                + "  density_offset = " + DENSITY_OFFSET + ";\n"
                + "  density_scale = " + DENSITY_SCALE + ";\n"
                + "  scale = " + intensity + ";\n"
                + "  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n"
                + "  vec4 color = texture2D(tex_sampler_0, vTextureCoord);\n"
                + "  float energy = dot(color.rgb, weights);\n"
                + "  float mask_value = energy - 0.5;\n"
                + "  float alpha;\n"
                + "  if (mask_value > 0.0) {\n"
                + "    alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;\n"
                + "  } else { \n"
                + "    alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;\n"
                + "  }\n"
                + "  float index = energy * hist_scale + hist_offset;\n"
                + "  vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));\n"
                + "  float value = temp.g + temp.r * shift_scale;\n"
                + "  index = value * density_scale + density_offset;\n"
                + "  temp = texture2D(tex_sampler_2, vec2(index, 0.5));\n"
                + "  value = temp.g + temp.r * shift_scale;\n"
                + "  float dst_energy = energy * alpha + value * (1.0 - alpha);\n"
                + "  float max_energy = energy / max(color.r, max(color.g, color.b));\n"
                + "  if (dst_energy > max_energy) {\n"
                + "    dst_energy = max_energy;\n"
                + "  }\n"
                + "  if (energy == 0.0) {\n"
                + "    gl_FragColor = color;\n"
                + "  } else {\n"
                + "    gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);\n"
                + "  }\n" + "}\n";
    }
}