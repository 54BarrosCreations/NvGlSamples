/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;

/**
 *
 * @author gbarbieri
 */
public class NvGLSLProgram extends glsl.GLSLProgramObject {

    public NvGLSLProgram(GL4 gl4, String shadersFilepath, String[] vertexShaders, String[] fragmentShaders) {
        super(gl4, shadersFilepath, vertexShaders, fragmentShaders);
    }

    public int getUniformLocation(GL4 gl4, String attribute) {

        int result = gl4.glGetUniformLocation(getProgramId(), "" + attribute);

        if (result == -1) {
            System.out.println("Could not find attribute " + attribute
                    + " in program " + getProgramId());
        }
        return result;
    }

    public int getAttribLocation(GL4 gl4, String attribute) {

        int result = gl4.glGetAttribLocation(getProgramId(), "" + attribute);

        if (result == -1) {
            System.out.println("Could not find attribute " + attribute
                    + " in program " + getProgramId());
        }
        return result;
    }
}
