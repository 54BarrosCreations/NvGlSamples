/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

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
