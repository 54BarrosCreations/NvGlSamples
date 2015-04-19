/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.util;

import com.jogamp.opengl.util.GLBuffers;

/**
 *
 * @author elect
 */
public class PerMeshUniforms {

    public float r;
    public float g;
    public float b;
    public float a;
    public float u;
    public float v;

    public static int size() {
        return 6 * GLBuffers.SIZEOF_FLOAT;
    }

    public float[] toFloatbuffer() {
        return new float[]{r, g, b, a, u, v};
    }
}
