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

import com.jogamp.opengl.math.FloatUtil;
import gl4_kepler.bindlessApp.Transform;
import jglm.Jglm;
import jglm.Mat4;
import nvAppBase.NvCameraXformType;
import jglm.Vec3;
import jglm.Vec4;

/**
 *
 * @author GBarbieri
 */
public class NvInputTransformer {

    private Transform[] xforms = new Transform[NvCameraXformType.COUNT.ordinal()];
    private int width;
    private int height;
    private NvCameraMotionType motionMode = NvCameraMotionType.ORBITAL;
    private int xVel_kb = 0;
    private int zVel_kb = 0;
    private int zVel_mouse = 0;
    private float xVel_gp = 0;
    private float zVel_gp = 0;

    public NvInputTransformer() {

        for (int i = 0; i < xforms.length; i++) {
            xforms[i] = new Transform();
            xforms[i].translateVel = new Vec3(0.0f, 0.0f, 0.0f);
            xforms[i].rotateVel = new Vec3(0.0f, 0.0f, 0.0f);
            xforms[i].translate = new Vec3(0.0f, 0.0f, 0.0f);
            xforms[i].rotate = new Vec3(0.0f, 0.0f, 0.0f);
            xforms[i].scale = 1.0f;
            xforms[i].dScale = 1.0f;
            xforms[i].maxRotationVel = (float) Math.PI;
            xforms[i].maxTranslationVel = 5.0f;
        }
    }

    public void update(float deltaTime) {

        if (motionMode == NvCameraMotionType.DUAL_ORBITAL) {
            Transform xfm = xforms[NvCameraXformType.MAIN.ordinal()];
            Transform xfs = xforms[NvCameraXformType.SECONDARY.ordinal()];
            xfm.rotate.plus(xfm.rotateVel.times(deltaTime));
            xfs.rotate.plus(xfs.rotateVel.times(deltaTime));
            xfm.translate.plus(xfm.translateVel.times(deltaTime));
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
            } else {
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

    public void setRotationVec(Vec3 vec) {
        setRotationVec(vec, NvCameraXformType.MAIN);
    }

    public void setRotationVec(Vec3 vec, NvCameraXformType xform) {
        xforms[xform.ordinal()].rotate = vec;
    }

    public void setTranslationVec(Vec3 vec) {
        xforms[NvCameraXformType.MAIN.ordinal()].translate = vec;
    }
    
    public void setScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Mat4 getModelViewMat() {
        Transform xf = xforms[NvCameraXformType.MAIN.ordinal()];
        if (motionMode == NvCameraMotionType.FIRST_PERSON) {
            return xf.rotateMat.mult(xf.translateMat.mult(xf.scaleMat));
        } else {
            return xf.translateMat.mult(xf.rotateMat.mult(xf.scaleMat));
        }
    }
}
