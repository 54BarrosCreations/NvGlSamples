/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import jglm.Mat4;
import jglm.Vec4;

/**
 *
 * @author gbarbieri
 */
public class NvMatrix {

    public static Mat4 rotationYawPitchRoll(float yaw, float pitch, float roll) {

        Mat4 m = new Mat4(1);

        if (roll != 0) {

            m = Mat4.rotationZ(roll);
        }
        if (pitch != 0) {

            m = m.mult(Mat4.rotationX(pitch));
        }
        if (yaw != 0) {

            m = m.mult(Mat4.rotationY(yaw));
        }
        return m;
    }
}
