/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import jglm.Mat4;
import jglm.Vec3;
import jglm.Vec4;

/**
 *
 * @author elect
 */
public class NvInputTransformer {

    private NvCameraMotionType motionMode;
    private Transform xforms[];
    private int xVel_kb;
    private int zVel_kb;
    private int zVel_mouse;
    private float xVel_gp;
    private float zVel_gp;

    public NvInputTransformer() {

        motionMode = NvCameraMotionType.ORBITAL;

        xforms = new Transform[NvCameraXformType.COUNT.ordinal()];
        for (int i = 0; i < NvCameraXformType.COUNT.ordinal(); i++) {
            xforms[i] = new Transform();

            xforms[i].scale = 1f;
            xforms[i].dScale = 1f;
            xforms[i].maxRotationVel = (float) Math.PI;
            xforms[i].maxTranslationVel = 5f;
        }
    }

    public void update(float deltaTime) {

        if (motionMode == NvCameraMotionType.DUAL_ORBITAL) {

            Transform xfm = xforms[NvCameraXformType.MAIN.ordinal()];
            Transform xfs = xforms[NvCameraXformType.SECONDARY.ordinal()];
            xfm.rotate = xfm.rotate.plus(xfm.rotateVel.times(deltaTime));
            xfs.rotate = xfs.rotate.plus(xfs.rotateVel.times(deltaTime));
            xfm.translate = xfm.translate.plus(xfm.translateVel.times(deltaTime));

            updateMats(NvCameraXformType.MAIN);
            updateMats(NvCameraXformType.SECONDARY);
        } else {
            Transform xf = xforms[NvCameraXformType.MAIN.ordinal()];
            xf.rotate = xf.rotate.plus(xf.rotateVel.times(deltaTime));
            Vec3 transVel;
            if (motionMode == NvCameraMotionType.FIRST_PERSON) {
                // obviously, this should clamp to [-1,1] for the mul, but we don't care for sample use.
                xf.translateVel.x = xf.maxTranslationVel * (xVel_kb + xVel_gp);
                xf.translateVel.z = xf.maxTranslationVel * (zVel_mouse + zVel_kb + zVel_gp);
                transVel = new Vec3(xf.rotateMat.transpose().mult(
                        new Vec4(-xf.translateVel.x, xf.translateVel.y, xf.translateVel.z, 0f)));
            }else{
                transVel = xf.translateVel;
            }
            
            xf.translate = xf.translate.plus(transVel.times(deltaTime));
            
            updateMats(NvCameraXformType.MAIN);
        }
    }

    private void updateMats(NvCameraXformType xform) {

        Transform xf = xforms[xform.ordinal()];
        xf.rotateMat = NvMatrix.rotationYawPitchRoll(xf.rotate.y, xf.rotate.x, 0f);
        xf.translateMat = Mat4.translate(xf.translate);
        xf.scaleMat = new Mat4(nvClampScale(xf.scale * xf.dScale));
        xf.scaleMat.c3.w = 1f;
    }

    private float nvClampScale(float s) {
        float nvMinScalePct = .035f;
        float nvMaxScalePct = 500f;
        return Math.min(Math.max(s, nvMinScalePct), nvMaxScalePct);
    }

    public Mat4 getModelViewMat() {
        Transform xf = xforms[NvCameraXformType.MAIN.ordinal()];
        if (motionMode == NvCameraMotionType.FIRST_PERSON) {
            return xf.rotateMat.mult(xf.translateMat.mult(xf.scaleMat));
        } else {
            return xf.translateMat.mult(xf.rotateMat.mult(xf.scaleMat));
        }
    }

    public void setTranslationVec(Vec3 vec) {
        xforms[NvCameraXformType.MAIN.ordinal()].translate = vec;
    }

    public void setRotationVec(Vec3 vec) {
        xforms[NvCameraXformType.MAIN.ordinal()].rotate = vec;
    }

    private enum NvCameraMotionType {

        ORBITAL, ///< Camera orbits the world origin
        FIRST_PERSON, ///< Camera moves as in a 3D, first-person shooter
        PAN_ZOOM, ///< Camera pans and zooms in 2D
        DUAL_ORBITAL ///< Two independent orbital transforms
    }

    private enum NvCameraXformType {

        MAIN, ///< Default transform
        SECONDARY, ///< Secondary transform
        COUNT ///< Number of transforms
    }
}
