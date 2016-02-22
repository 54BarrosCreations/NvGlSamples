/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp;

import glm.mat._4.Mat4;

/**
 *
 * @author GBarbieri
 */
public class TransformUniforms {

    public Mat4 modelView = new Mat4();
    public Mat4 modelViewProjection = new Mat4();
    public int useBindlessUniforms;

    public static final int SIZE = 2 * Mat4.SIZE + Integer.BYTES;
    public static final int USEBINDLESSUNIFORM_OFFSET = 2 * Mat4.SIZE;
}
