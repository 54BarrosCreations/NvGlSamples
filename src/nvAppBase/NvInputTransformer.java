//----------------------------------------------------------------------------------
// File:        gl4-kepler/BindlessApp/BindlessApp.cpp
// SDK Version: v2.11 
// Email:       gameworks@nvidia.com
// Site:        http://developer.nvidia.com/
//
// Copyright (c) 2014-2015, NVIDIA CORPORATION. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//----------------------------------------------------------------------------------
package nvAppBase;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

/**
 *
 * @author GBarbieri
 */
public class NvInputTransformer implements MouseListener {

    private Transform[] xforms = new Transform[NvCameraXformType.COUNT];
    private int width;
    private int height;
    private int motionMode = NvCameraMotionType.ORBITAL;
    private int xVel_kb = 0;
    private int zVel_kb = 0;
    private int zVel_mouse = 0;
    private float xVel_gp = 0;
    private float zVel_gp = 0;
    private boolean touchDown = false;
    private int maxPointsCount = 0;
    private int mode = ControlMode.TRANSLATE;
    private Vec2 lastInput = new Vec2();
    private Vec2 firstInput = new Vec2();

    public NvInputTransformer() {

        for (int i = 0; i < xforms.length; i++) {
            xforms[i] = new Transform();
            xforms[i].translateVel = new Vec3();
            xforms[i].rotateVel = new Vec3();
            xforms[i].translate = new Vec3();
            xforms[i].rotate = new Vec3();
            xforms[i].scale = 1.0f;
            xforms[i].scaleD = 1.0f;
            xforms[i].maxRotationVel = (float) Math.PI;
            xforms[i].maxTranslationVel = 5.0f;
        }
    }

    public void update(float deltaTime) {
        if (motionMode == NvCameraMotionType.DUAL_ORBITAL) {
            Transform xfm = xforms[NvCameraXformType.MAIN];
            Transform xfs = xforms[NvCameraXformType.SECONDARY];
            xfm.rotate.add(xfm.rotateVel.mul(deltaTime));
            xfs.rotate.add(xfs.rotateVel.mul(deltaTime));
            xfm.translate.add(xfm.translateVel.mul(deltaTime));

            updateMats(NvCameraXformType.MAIN);
            updateMats(NvCameraXformType.SECONDARY);
        } else {
            Transform xf = xforms[NvCameraXformType.MAIN];
            xf.rotate = xf.rotate.add(xf.rotateVel.mul(deltaTime));
            Vec3 transVel;
            if (motionMode == NvCameraMotionType.FIRST_PERSON) {
                // obviously, this should clamp to [-1,1] for the mul, but we don't care for sample use.
                xf.translateVel.x = xf.maxTranslationVel * (xVel_kb + xVel_gp);
                xf.translateVel.z = xf.maxTranslationVel * (zVel_mouse + zVel_kb + zVel_gp);
                transVel = new Vec3(glm.transpose_(xf.rotateMat).mul(
                        new Vec4(-xf.translateVel.x, xf.translateVel.y, xf.translateVel.z, 0f)));
            } else {
                transVel = xf.translateVel;
            }

            xf.translate.add(transVel.mul(deltaTime));

            updateMats(NvCameraXformType.MAIN);
        }
    }

    private void updateMats(int xform) {

        Transform xf = xforms[xform];
        NvMatrix.rotationYawPitchRoll(xf.rotateMat, xf.rotate.y, xf.rotate.x, 0f);
        xf.translateMat.translation(xf.translate);
        xf.scaleMat.identity();
        xf.scaleMat.scale(nvClampScale(xf.scale * xf.scaleD));
    }

    private float nvClampScale(float s) {
        float nvMinScalePct = .035f;
        float nvMaxScalePct = 500f;
        return Math.min(Math.max(s, nvMinScalePct), nvMaxScalePct);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        processPoint(e, NvPointerActionType.DOWN);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        processPoint(e, NvPointerActionType.UP);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        processPoint(e, NvPointerActionType.MOTION);
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
    }

    private boolean processPoint(MouseEvent e, int action) {

        Transform xfm = xforms[NvCameraXformType.MAIN];
        Transform xfs = xforms[NvCameraXformType.SECONDARY];
        float x = e.getX();
        float y = e.getY();
        boolean needsUpdate = false;

        switch (action) {
            case NvPointerActionType.UP:
                touchDown = true;
                maxPointsCount = 1;
                // lock in scaling
                if (motionMode != NvCameraMotionType.FIRST_PERSON) {
                    xfm.scale = nvClampScale(xfm.scale * xfm.scaleD);
                    xfm.scaleD = 1.0f;
                }
                break;
            case NvPointerActionType.MOTION:
                if (touchDown) {
                    if (motionMode == NvCameraMotionType.FIRST_PERSON) {
                        //...
                    } else if (maxPointsCount == 1) {
                        switch (mode) {
                            case ControlMode.ROTATE:
                                xfm.rotate.x += ((y - lastInput.y) / height) * xfm.maxRotationVel;
                                xfm.rotate.y += ((x - lastInput.x) / width) * xfm.maxRotationVel;
                                needsUpdate = true;
                                break;
                            case ControlMode.TRANSLATE:
                                xfm.translate.x += ((x - lastInput.x) / width) * xfm.maxTranslationVel;
                                xfm.translate.y -= ((y - lastInput.y) / height) * xfm.maxTranslationVel;
                                needsUpdate = true;
                                break;
                            case ControlMode.ZOOM:
                                float dy = y - firstInput.y; // negative for moving up, positive moving down.
                                if (dy > 0) // scale up...
                                {
                                    xfm.scaleD = 1.0f + dy / (height / 16.0f);
                                } else if (dy < 0) { // scale down...
                                    xfm.scaleD = 1.0f - (-dy / (height / 2));
                                    xfm.scaleD *= xfm.scaleD; // accelerate the shrink...
                                }
                                xfm.scaleD = nvClampScale(xfm.scaleD);
                                needsUpdate = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
                if ((maxPointsCount == 1) || (motionMode == NvCameraMotionType.FIRST_PERSON)
                        || (motionMode == NvCameraMotionType.DUAL_ORBITAL)) {
                    lastInput.x = e.getX();
                    lastInput.y = e.getY();
                }
                break;

            default:
                // down
                if (action == NvPointerActionType.DOWN) {
                    touchDown = true;
                    maxPointsCount = 1;
                    xfm.scaleD = 1.0f; // for sanity reset to 1.

                    if (motionMode == NvCameraMotionType.PAN_ZOOM) {
                        mode = ControlMode.TRANSLATE;
                    } else {
                        mode = ControlMode.ROTATE;
                    }
                    if (motionMode != NvCameraMotionType.FIRST_PERSON) {
                        if (motionMode == NvCameraMotionType.ORBITAL) {
                            if (e.getButton() == MouseEvent.BUTTON2) {
                                mode = ControlMode.ZOOM;
                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                mode = ControlMode.TRANSLATE;
                            }
                        } else if (motionMode == NvCameraMotionType.DUAL_ORBITAL) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                mode = ControlMode.ROTATE;
                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                mode = ControlMode.SECONDARY_ROTATE;
                            }
                        } else if (e.getButton() == MouseEvent.BUTTON3) { // PAN_ZOOM
                            mode = ControlMode.ZOOM;
                        }
                    }
                    firstInput.x = e.getX();
                    firstInput.y = e.getY();
                }
                break;
        }
        if ((maxPointsCount == 1) || (motionMode == NvCameraMotionType.FIRST_PERSON)
                || (motionMode == NvCameraMotionType.DUAL_ORBITAL)) {
            lastInput.x = e.getX();
            lastInput.y = e.getY();
        }
        return true;
    }

    public void setScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setRotationVec(Vec3 vec) {
        setRotationVec(vec, NvCameraXformType.MAIN);
    }

    public void setRotationVec(Vec3 vec, int xform) {
        xforms[xform].rotate = vec;
    }

    public void setTranslationVec(Vec3 vec) {
        setTranslationVec(vec, NvCameraXformType.MAIN);
    }

    public void setTranslationVec(Vec3 vec, int xform) {
        xforms[xform].translate = vec;
    }

    public Mat4 getModelViewMat() {
        return getModelViewMat(NvCameraXformType.MAIN);
    }

    public Mat4 getModelViewMat(int xform) {
        Transform xf = xforms[xform];
        if (motionMode == NvCameraMotionType.FIRST_PERSON) {
            return xf.rotateMat.mul_(xf.translateMat).mul(xf.scaleMat);
        } else {
            return xf.translateMat.mul_(xf.rotateMat).mul(xf.scaleMat);
        }
    }

    private class NvCameraMotionType {

        public static final int ORBITAL = 0; ///< Camera orbits the world origin
        public static final int FIRST_PERSON = 1; ///< Camera moves as in a 3D, first-person shooter
        public static final int PAN_ZOOM = 2; ///< Camera pans and zooms in 2D
        public static final int DUAL_ORBITAL = 3;  ///< Two independent orbital transforms
    }

    private class NvCameraXformType {

        public static final int MAIN = 0; ///< Default transform
        public static final int SECONDARY = 1; ///< Secondary transform
        public static final int COUNT = 2; ///< Number of transforms
    }

    private class ControlMode {

        public static final int TRANSLATE = 0;
        public static final int ROTATE = 1;
        public static final int SECONDARY_ROTATE = 2;
        public static final int ZOOM = 3;
    }

    private class NvPointerActionType {

        public static final int UP = 0; ///< touch or button release
        public static final int DOWN = 1; ///< touch or button press
        public static final int MOTION = 2; ///< touch or mouse pointer movement
        public static final int EXTRA_DOWN = 4; ///< multitouch additional touch press
        public static final int EXTRA_UP = 8; ///< multitouch additional touch release
    }

    private class Transform {

        public Vec3 translateVel;
        public Vec3 rotateVel;
        public float maxRotationVel;
        public float maxTranslationVel;

        public Vec3 translate;
        public Vec3 rotate;
        public float scale, scaleD;

        public Mat4 translateMat = new Mat4();
        public Mat4 rotateMat = new Mat4();
        public Mat4 scaleMat = new Mat4();
    }
}
