/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp;

import jglm.Mat4;

/**
 *
 * @author GBarbieri
 */
public class TransformUniforms {

    public Mat4 modelView;
    public Mat4 modelViewProjection;
    public int useBindlessUniforms;

    public static final int SIZEOF = 16 * 2 * Float.BYTES + Integer.BYTES;
}
