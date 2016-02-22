/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvAppBase;

import glm.mat._4.Mat4;

/**
 *
 * @author gbarbieri
 */
public class NvMatrix {

    public static Mat4 rotationYawPitchRoll(Mat4 m, float yaw, float pitch, float roll) {

        m.identity();

        if (roll != 0) {

            m.rotationZ(roll);
        }
        if (pitch != 0) {

            m.rotateX(pitch);
        }
        if (yaw != 0) {

            m.rotateY(yaw);
        }
        return m;
    }
}
