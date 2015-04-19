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

    public Vec3 translateVel;
    public Vec3 rotateVel;
    public float maxRotationVel;
    public float maxTranslationVel;

    public Vec3 translate;
    public Vec3 rotate;
    public float scale;

    public Mat4 translateMat;
    public Mat4 rotateMat;
    public Mat4 scaleMat;
    
    public Transform() {
        
        translateVel = new Vec3();
        rotateVel = new Vec3();
        
        translate = new Vec3();
        rotate = new Vec3();
        
        translateMat = new Mat4(1f);
        rotateMat = new Mat4(1f);
        scaleMat = new Mat4(1f);
    }
}
