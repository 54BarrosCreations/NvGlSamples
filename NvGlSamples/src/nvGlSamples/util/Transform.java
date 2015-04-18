/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import jglm.Mat4;
import jglm.Vec3;

/**
 *
 * @author elect
 */
public class Transform {

    private Vec3 translateVel;
    private Vec3 rotateVel;
    private float maxRotationVel;
    private float maxTranslationVel;

    public Vec3 translate;
    public Vec3 rotate;
    public float scale;

    private Mat4 translateMat;
    private Mat4 rotateMat;
    private Mat4 scaleMat;
}
