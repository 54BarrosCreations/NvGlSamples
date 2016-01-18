/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp;

import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 *
 * @author elect
 */
public class NvGLSLProgram {

    private int program;
    private boolean logAllMissing = false;
    private boolean strict;

    private NvGLSLProgram(int program, boolean strict) {
        this.program = program;
        this.strict = strict;
    }

    public static NvGLSLProgram createFromFiles(GL4 gl4, String root, String shaderName) {
        return createFromFiles(gl4, root, shaderName, false);
    }

    public static NvGLSLProgram createFromFiles(GL4 gl4, String root, String shaderName, boolean strict) {

        ShaderProgram shaderProgram = new ShaderProgram();

        ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                NvGLSLProgram.class, root, null, shaderName, "vert", null, true);
        ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                NvGLSLProgram.class, root, null, shaderName, "frag", null, true);

        shaderProgram.add(vertShaderCode);
        shaderProgram.add(fragShaderCode);
        shaderProgram.link(gl4, System.out);
        shaderProgram.program();

        return new NvGLSLProgram(shaderProgram.program(), strict);
    }

    public int getAttribLocation(GL4 gl4, String attribute, boolean isOptional) {

        int result = gl4.glGetAttribLocation(program, attribute);

        if (result == -1) {
            if ((logAllMissing || strict) && !isOptional) {
                System.err.println("could not find attribute " + attribute + " in program " + program);
            }
        }

        return result;
    }
}
