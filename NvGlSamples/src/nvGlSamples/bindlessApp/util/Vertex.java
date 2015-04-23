/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.util;

import com.jogamp.opengl.util.GLBuffers;
import jglm.Jglm;

/**
 *
 * @author elect
 */
public class Vertex {

    public float[] position = new float[3];
    public byte[] color = new byte[4];
    public float[] attrib0 = new float[4];
    public float[] attrib1 = new float[4];
    public float[] attrib2 = new float[4];
    public float[] attrib3 = new float[4];
    public float[] attrib4 = new float[4];
    public float[] attrib5 = new float[4];
    public float[] attrib6 = new float[4];

    public static final int positionOffset = 0;
    public static final int colorOffset = 12;
    public static final int attrib0Offset = 16;
    public static final int attrib1Offset = 32;
    public static final int attrib2Offset = 48;
    public static final int attrib3Offset = 64;
    public static final int attrib4Offset = 72;
    public static final int attrib5Offset = 96;
    public static final int attrib6Offset = 112;

    public Vertex(float x, float y, float z, float r, float g, float b, float a) {

        position[0] = x;
        position[1] = y;
        position[2] = z;

        color[0] = (byte) (Jglm.clamp(r, 0f, 1f) * 255.5f);
        color[1] = (byte) (Jglm.clamp(g, 0f, 1f) * 255.5f);
        color[2] = (byte) (Jglm.clamp(b, 0f, 1f) * 255.5f);
        color[3] = (byte) (Jglm.clamp(a, 0f, 1f) * 255.5f);
    }

    public static int size() {
        return 3 * GLBuffers.SIZEOF_FLOAT + 4 * GLBuffers.SIZEOF_BYTE + 4 * 7 * GLBuffers.SIZEOF_FLOAT;
    }

    public float[] toFloatArray() {

        float[] result = new float[3 + 1 + 4 * 7];

        System.arraycopy(position, 0, result, 0, position.length);
        result[3] = (color[0] & 0xff) << 24 | (color[1] & 0xff) << 16
                | (color[2] & 0xff) << 8 | (color[3] & 0xff);
        System.arraycopy(attrib0, 0, result, 4 + 4 * 0, attrib0.length);
        System.arraycopy(attrib1, 0, result, 4 + 4 * 1, attrib1.length);
        System.arraycopy(attrib2, 0, result, 4 + 4 * 2, attrib2.length);
        System.arraycopy(attrib3, 0, result, 4 + 4 * 3, attrib3.length);
        System.arraycopy(attrib4, 0, result, 4 + 4 * 4, attrib4.length);
        System.arraycopy(attrib5, 0, result, 4 + 4 * 5, attrib5.length);
        System.arraycopy(attrib6, 0, result, 4 + 4 * 6, attrib6.length);

        return result;
    }
}
