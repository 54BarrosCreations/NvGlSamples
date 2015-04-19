/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.util;

import com.jogamp.opengl.util.GLBuffers;
import jglm.Mat4;

/**
 *
 * @author elect
 */
public class TransformUniforms {

    public Mat4 modelView;
    public Mat4 modelViewProjection;
    public int useBindlessUniforms;

    public static int size() {
        return 16 * 4 * 2 * GLBuffers.SIZEOF_FLOAT + 1 * GLBuffers.SIZEOF_INT;
    }

    public float[] toFloatarray() {

        float[] floatarray = new float[16 * 2 + 1];

        System.arraycopy(modelView.toFloatArray(), 0, floatarray, 0, 16);
        System.arraycopy(modelViewProjection.toFloatArray(), 0, floatarray, 16, 16);
        floatarray[16 * 2] = useBindlessUniforms;
        
        return floatarray;
    }
}
