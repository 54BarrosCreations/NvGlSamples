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
package nvGlSamples.bindlessApp;

import nvGlSamples.bindlessApp.util.PerMeshUniforms;
import nvGlSamples.bindlessApp.util.Mesh;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import glutil.ViewData;
import glutil.ViewPole;
import glutil.ViewScale;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jglm.Jglm;
import jglm.Mat4;
import jglm.Quat;
import jglm.Vec2i;
import jglm.Vec3;
import nvGlSamples.bindlessApp.assets.Program;
import nvGlSamples.util.NvInputTransformer;

// This sample demonstrates using bindless rendering with GL_NV_shader_buffer_load and GL_NV_vertex_buffer_unified_memory.
// GL_NV_shader_buffer_load allows the program to pass a GPU pointer to the vertex shader to load uniforms directly from GPU memory.
// GL_NV_vertex_buffer_unified_memory allows the program to use GPU pointers to vertex and index data when making rendering calls.
// Both of these extensions can significantly reduce CPU L2 cache misses and pollution; this can dramatically speed up scenes with
// large numbers of draw calls.
//
// For more detailed information see http://www.nvidia.com/object/bindless_graphics.html
//
//
//
// Interesting pieces of code are annotated with "*** INTERESTING ***"
//
// The interesting code in this sample is in this file and Mesh.cpp
//
// Mesh::update() in Mesh.cpp contains the source code for getting the GPU pointers for vertex and index data
// Mesh::renderPrep() in Mesh.cpp sets up the vertex format
// Mesh::render() in Mesh.cpp does the actual rendering
// Mesh::renderFinish() in Mesh.cpp resets related state
/**
 *
 * @author gbarbieri
 */
public class GlViewer implements GLEventListener {

    private Vec2i imageSize;
    private GLWindow glWindow;
    private NewtCanvasAWT newtCanvasAWT;
    private Animator animator;
    private ViewPole viewPole;
    private int[] ubo;
    private MouseListener mouseListener;
    public static float projectionBase;
    private Scene scene;
    private NvInputTransformer transformer;
    private Program shader;
    private final int SQRT_BUILDING_COUNT = 100;
    private ArrayList<Mesh> meshes;
    private ArrayList<PerMeshUniforms> perMeshUniformsData;

    public GlViewer() {

        imageSize = new Vec2i(1024, 768);

        initGL();
    }

    private void initGL() {

        GLProfile gLProfile = GLProfile.getDefault();

        GLCapabilities gLCapabilities = new GLCapabilities(gLProfile);

        glWindow = GLWindow.create(gLCapabilities);

        newtCanvasAWT = new NewtCanvasAWT(glWindow);

        glWindow.setSize(imageSize.x, imageSize.y);

        glWindow.addGLEventListener(this);

        animator = new Animator(glWindow);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable glad) {
        System.out.println("init");

        GL4 gl4 = glad.getGL().getGL4();

        // Check extensions; exit on failure
        checkExtenstions(gl4);

        // Create our pixel and vertex shader
        shader = new Program(gl4, "/nvGlSamples/bindlessApp/assets/shaders/",
                new String[]{"simple_vertex.glsl"}, new String[]{"simple_fragment.glsl"});

        // Set the initial view
        transformer.setRotationVec(new Vec3((float) Math.toRadians(30f),
                (float) Math.toRadians(30f), 0));

        // Create the meshes
        meshes = new ArrayList<>(1 + SQRT_BUILDING_COUNT * SQRT_BUILDING_COUNT);
        perMeshUniformsData = new ArrayList<>(meshes.size());
        
        // Create a mesh for the ground
        meshes.set(0, createGround(new Vec3(0f, -.001f, 0f), new Vec3(5f, 0f, 5f)));
        
        // Create "building" meshes
        int meshIndex = 0;

        for (int i = 0; i < SQRT_BUILDING_COUNT; i++) {

            for (int j = 0; j < SQRT_BUILDING_COUNT; j++) {

                float x, z, radius;

                x = (float) i / (float) SQRT_BUILDING_COUNT - .5f;
                z = (float) j / (float) SQRT_BUILDING_COUNT - .5f;
                radius = (float) Math.sqrt(x * x + z * z);
            }
        }

        try {
            scene = new Scene(gl4, "/data/dragon.obj");
        } catch (IOException ex) {
            Logger.getLogger(GlViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        Vec3 target = new Vec3(0f, .12495125f, 0f);
        Quat orient = new Quat(0.0f, 0.0f, 0.0f, 1.0f);
        ViewData initialViewData = new ViewData(target, orient, 0.5f, 0.0f);

        ViewScale viewScale = new ViewScale(3.0f, 20.0f, 1.5f, 0.0005f, 0.0f, 0.0f, 90.0f / 250.0f);

        viewPole = new ViewPole(initialViewData, viewScale, ViewPole.Projection.perspective);

        mouseListener = new MouseListener(viewPole);
        glWindow.addMouseListener(mouseListener);

        int blockBinding = 0;

        initUBO(gl4, blockBinding);

//        weightedBlended = new WeightedBlended(gl4, blockBinding);
        gl4.glDisable(GL4.GL_CULL_FACE);

        projectionBase = 5000f;

        animator.setUpdateFPSFrames(60, System.out);

        checkError(gl4);
    }

    private void checkExtenstions(GL4 gl4) {

        boolean GL_NV_vertex_buffer_unified_memory = gl4.isExtensionAvailable("GL_NV_vertex_buffer_unified_memory");
        if (!GL_NV_vertex_buffer_unified_memory) {
            System.out.println("GL_NV_vertex_buffer_unified_memory not available");
        }
        boolean GL_NV_shader_buffer_load = gl4.isExtensionAvailable("GL_NV_shader_buffer_load");
        if (!GL_NV_shader_buffer_load) {
            System.out.println("GL_NV_shader_buffer_load not available");
        }
        boolean GL_EXT_direct_state_access = gl4.isExtensionAvailable("GL_EXT_direct_state_access");
        if (!GL_EXT_direct_state_access) {
            System.out.println("GL_EXT_direct_state_access not available");
        }
        boolean GL_NV_bindless_texture = gl4.isExtensionAvailable("GL_NV_bindless_texture");
        if (!GL_NV_bindless_texture) {
            System.out.println("GL_NV_bindless_texture not available");
        }
    }

    private Mesh createGround(Vec3 pos, Vec3 dim) {
        
        
    }
    
    private void initUBO(GL4 gl4, int blockBinding) {

        ubo = new int[1];
        int size = 16 * GLBuffers.SIZEOF_FLOAT;

        gl4.glGenBuffers(1, ubo, 0);
        gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, ubo[0]);
        {
            gl4.glBufferData(GL4.GL_UNIFORM_BUFFER, size * 2, null, GL4.GL_DYNAMIC_DRAW);

            gl4.glBindBufferBase(GL4.GL_UNIFORM_BUFFER, blockBinding, ubo[0]);
        }
        gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, 0);
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        System.out.println("dispose");
    }

    @Override
    public void display(GLAutoDrawable glad) {
//        System.out.println("display");

        GL4 gl4 = glad.getGL().getGL4();

        updateCamera(gl4);

//        weightedBlended.render(gl4, scene);
        checkError(gl4);
    }

    private void updateCamera(GL4 gl4) {

        gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, ubo[0]);
        {
            int size = 16 * GLBuffers.SIZEOF_FLOAT;
            int offset = 0;

            FloatBuffer viewMat = GLBuffers.newDirectFloatBuffer(viewPole.calcMatrix().toFloatArray());

            gl4.glBufferSubData(GL4.GL_UNIFORM_BUFFER, offset, size, viewMat);
        }
        gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, 0);
    }

    private void checkError(GL4 gl4) {

        int error = gl4.glGetError();

        if (error != GL4.GL_NO_ERROR) {
            System.out.println("error " + error);
        }
    }

    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
        System.out.println("reshape");

        GL4 gl4 = glad.getGL().getGL4();

//        weightedBlended.reshape(gl4, width, height);
        imageSize = new Vec2i(width, height);

        updateProjection(gl4, width, height);

        gl4.glViewport(0, 0, width, height);

        checkError(gl4);
    }

    private void updateProjection(GL4 gl3, int width, int height) {

        gl3.glBindBuffer(GL4.GL_UNIFORM_BUFFER, ubo[0]);
        {
            float aspect = (float) width / (float) height;
            int size = 16 * GLBuffers.SIZEOF_FLOAT;
            int offset = size;

            Mat4 projMat = Jglm.perspective(60f, aspect, 0.0001f, 10);
            FloatBuffer projFB = GLBuffers.newDirectFloatBuffer(projMat.toFloatArray());

            gl3.glBufferSubData(GL4.GL_UNIFORM_BUFFER, offset, size, projFB);
        }
        gl3.glBindBuffer(GL4.GL_UNIFORM_BUFFER, 0);
    }

    public NewtCanvasAWT getNewtCanvasAWT() {
        return newtCanvasAWT;
    }

    public GLWindow getGlWindow() {
        return glWindow;
    }

    public Animator getAnimator() {
        return animator;
    }
}
