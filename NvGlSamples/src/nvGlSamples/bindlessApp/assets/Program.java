/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.assets;

import com.jogamp.opengl.GL4;

/**
 *
 * @author elect
 */
public class Program extends glsl.GLSLProgramObject {

    private final int bindlessPerMeshUniformsPtrAttribLocation;

    public Program(GL4 gl4, String shadersFilepath, String[] vertexShaders, String[] fragmentShaders) {

        super(gl4, shadersFilepath, vertexShaders, fragmentShaders);

        bindlessPerMeshUniformsPtrAttribLocation = gl4.glGetAttribLocation(getProgramId(),
                "bindlessPerMeshUniformsPtr");
    }

    public int getBindlessPerMeshUniformsPtrAttribLocation() {
        return bindlessPerMeshUniformsPtrAttribLocation;
    }
}
