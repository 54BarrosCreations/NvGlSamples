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
public class NvInputTransformer {

    private NvCameraMotionType motionMode;
    private Transform xforms[];

    public NvInputTransformer() {

        motionMode = NvCameraMotionType.ORBITAL;

        xforms = new Transform[NvCameraXformType.COUNT.ordinal()];
        for (int i = 0; i < NvCameraXformType.COUNT.ordinal(); i++) {
            xforms[i] = new Transform();
            
            xforms[i].scale = 1f;
            xforms[i].maxRotationVel = (float)Math.PI;
            xforms[i].maxTranslationVel = 5f;
        }
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
