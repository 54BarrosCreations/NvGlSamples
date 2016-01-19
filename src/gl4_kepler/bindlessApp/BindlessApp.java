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
package gl4_kepler.bindlessApp;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_NUM_EXTENSIONS;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import jglm.Vec2;
import jglm.Vec3;

/**
 *
 * @author elect
 */
public class BindlessApp implements GLEventListener, KeyListener {

    private static int screenIdx = 0;
    private static Dimension windowSize = new Dimension(1024, 768);
    private static boolean undecorated = false;
    private static boolean alwaysOnTop = false;
    private static boolean fullscreen = false;
    private static boolean mouseVisible = true;
    private static boolean mouseConfined = false;
    public static GLWindow glWindow;
    public static Animator animator;
    private static String title = "Bindless Graphics Sample";

    public static void main(String[] args) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(windowSize.getWidth(), windowSize.getHeight());
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.setTitle(title);
        glWindow.setVisible(true);

        BindlessApp bindlessApp = new BindlessApp();
        glWindow.addGLEventListener(bindlessApp);
        glWindow.addKeyListener(bindlessApp);

        animator = new Animator(glWindow);
        animator.setRunAsFastAsPossible(true);
        animator.setUpdateFPSFrames(1000, System.out);
        animator.start();
    }

    private final int SQRT_BUILDING_COUNT = 100;

    // Simple collection of meshes to render
    private Mesh[] meshes;

    // Shader stuff
    private NvGLSLProgram shader;
    private int bindlessPerMeshUniformsPtrAttribLocation;

    // uniform buffer object (UBO) for tranform data
    private int[] transformUniforms = {0};

    // uniform buffer object (UBO) for mesh param data
    private int[] perMeshUniforms = {0};
    private PerMeshUniforms[] perMeshUniformsData;

    private NvInputTransformer transformer = new NvInputTransformer();

    public BindlessApp() {

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        System.out.println("init");

        GL4 gl4 = drawable.getGL().getGL4();

        // Check extensions; exit on failure
        if (!requireExtension(gl4, "GL_NV_vertex_buffer_unified_memory")) {
            return;
        }
        if (!requireExtension(gl4, "GL_NV_shader_buffer_load")) {
            return;
        }
        if (!requireExtension(gl4, "GL_EXT_direct_state_access")) {
            return;
        }
        if (!requireExtension(gl4, "GL_NV_bindless_texture")) {
            return;
        }

        // Create our pixel and vertex shader
        shader = NvGLSLProgram.createFromFiles(gl4, "src/gl4_kepler/bindlessApp/shaders", "bindless");
        bindlessPerMeshUniformsPtrAttribLocation
                = shader.getAttribLocation(gl4, "bindlessPerMeshUniformsPtr", true);
        System.out.println("bindlessPerMeshUniformsPtrAttribLocation " + bindlessPerMeshUniformsPtrAttribLocation);

        // Set the initial view
        transformer.setRotationVec(new Vec3((float) Math.toRadians(30.0f), (float) Math.toRadians(30.0f), 0.0f));

        // Create the meshes
        meshes = new Mesh[1 + SQRT_BUILDING_COUNT * SQRT_BUILDING_COUNT];
        perMeshUniformsData = new PerMeshUniforms[meshes.length];

        // Create a mesh for the ground
        meshes[0] = createGround(gl4, new Vec3(0.f, -.001f, 0.f), new Vec3(5.0f, 0.0f, 5.0f));

        // Create "building" meshes
        int meshIndex = 0;
        for (int i = 0; i < SQRT_BUILDING_COUNT; i++) {

            for (int k = 0; k < SQRT_BUILDING_COUNT; k++) {

                float x, y, z;
                float size;

                x = (float) i / SQRT_BUILDING_COUNT - 0.5f;
                y = 0.0f;
                z = (float) k / SQRT_BUILDING_COUNT - 0.5f;
                size = .025f * (100.0f / SQRT_BUILDING_COUNT);

                meshes[meshIndex + 1] = createBuilding(gl4, new Vec3(5.0f * x, y, 5.0f * z),
                        new Vec3(size, (float) (0.2f + .1f * Math.sin(5.0f * i * k)), size),
                        new Vec2((float) k / SQRT_BUILDING_COUNT, (float) i / SQRT_BUILDING_COUNT));

                meshIndex++;
            }
        }
        /**
         * TODO.
         */
        // Initialize Bindless Textures
//	InitBindlessTextures();

        // create Uniform Buffer Object (UBO) for transform data and initialize 
        gl4.glCreateBuffers(1, transformUniforms, 0);
        gl4.glNamedBufferData(transformUniforms[0], TransformUniforms.SIZEOF, null, GL_STREAM_DRAW);

        // create Uniform Buffer Object (UBO) for param data and initialize
        gl4.glGenBuffers(1, perMeshUniforms, 0);
    }

    /**
     * Create a very simple building mesh.
     */
    private Mesh createBuilding(GL4 gl4, Vec3 pos, Vec3 dim, Vec2 uv) {
        Vertex[] vertices = new Vertex[4 * 6];
        short[] indices = new short[6 * 6];
        float r, g, b;

        dim.x *= 0.5f;
        dim.z *= 0.5f;

        // Generate a simple building model (i.e. a box). All of the "buildings" are in world space
        // +Z face
        r = randomColor();
        g = randomColor();
        b = randomColor();
        vertices[0] = new Vertex(-dim.x + pos.x, 0.0f + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[1] = new Vertex(+dim.x + pos.x, 0.0f + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[2] = new Vertex(+dim.x + pos.x, dim.y + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[3] = new Vertex(-dim.x + pos.x, dim.y + pos.y, +dim.z + pos.z, r, g, b, 1.0f);

        // -Z face
        r = randomColor();
        g = randomColor();
        b = randomColor();
        vertices[4] = new Vertex(-dim.x + pos.x, dim.y + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[5] = new Vertex(+dim.x + pos.x, dim.y + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[6] = new Vertex(+dim.x + pos.x, 0.0f + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[7] = new Vertex(-dim.x + pos.x, 0.0f + pos.y, -dim.z + pos.z, r, g, b, 1.0f);

        // +X face
        r = randomColor();
        g = randomColor();
        b = randomColor();
        vertices[8] = new Vertex(+dim.x + pos.x, 0.0f + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[9] = new Vertex(+dim.x + pos.x, 0.0f + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[10] = new Vertex(+dim.x + pos.x, dim.y + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[11] = new Vertex(+dim.x + pos.x, dim.y + pos.y, +dim.z + pos.z, r, g, b, 1.0f);

        // -X face
        r = randomColor();
        g = randomColor();
        b = randomColor();
        vertices[12] = new Vertex(-dim.x + pos.x, dim.y + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[13] = new Vertex(-dim.x + pos.x, dim.y + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[14] = new Vertex(-dim.x + pos.x, 0.0f + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[15] = new Vertex(-dim.x + pos.x, 0.0f + pos.y, +dim.z + pos.z, r, g, b, 1.0f);

        // +Y face
        r = randomColor();
        g = randomColor();
        b = randomColor();
        vertices[16] = new Vertex(-dim.x + pos.x, dim.y + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[17] = new Vertex(+dim.x + pos.x, dim.y + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[18] = new Vertex(+dim.x + pos.x, dim.y + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[19] = new Vertex(-dim.x + pos.x, dim.y + pos.y, -dim.z + pos.z, r, g, b, 1.0f);

        // -Y face
        r = randomColor();
        g = randomColor();
        b = randomColor();
        vertices[20] = new Vertex(-dim.x + pos.x, 0.0f + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[21] = new Vertex(+dim.x + pos.x, 0.0f + pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[22] = new Vertex(+dim.x + pos.x, 0.0f + pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[23] = new Vertex(-dim.x + pos.x, 0.0f + pos.y, +dim.z + pos.z, r, g, b, 1.0f);

        // Create the indices
        for (int i = 0; i < 6; i++) {

            for (int j = 0; j < 24; j += 4) {
                indices[i * 6 + 0] = (short) (0 + j);
                indices[i * 6 + 1] = (short) (1 + j);
                indices[i * 6 + 2] = (short) (2 + j);

                indices[i * 6 + 3] = (short) (0 + j);
                indices[i * 6 + 4] = (short) (2 + j);
                indices[i * 6 + 5] = (short) (3 + j);
            }
        }

        Mesh building = new Mesh();
        building.update(gl4, vertices, indices);

        return building;
    }

    /**
     * Generates a random color.
     */
    private float randomColor() {
        return (float) (((1 - Math.random()) % 255) / 255.0f);
    }

    /**
     * Create a mesh for the ground.
     */
    private Mesh createGround(GL4 gl4, Vec3 pos, Vec3 dim) {

        Vertex[] vertices = new Vertex[4];
        short[] indices;
        float r, g, b;

        dim.x *= 0.5f;
        dim.z *= 0.5f;

        // +Y face
        r = 0.3f;
        g = 0.3f;
        b = 0.3f;
        vertices[0] = new Vertex(-dim.x + pos.x, pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[1] = new Vertex(+dim.x + pos.x, pos.y, +dim.z + pos.z, r, g, b, 1.0f);
        vertices[2] = new Vertex(+dim.x + pos.x, pos.y, -dim.z + pos.z, r, g, b, 1.0f);
        vertices[3] = new Vertex(-dim.x + pos.x, pos.y, -dim.z + pos.z, r, g, b, 1.0f);

        // Create the indices
        indices = new short[]{
            0, 1, 2,
            0, 2, 3};

        Mesh ground = new Mesh();
        ground.update(gl4, vertices, indices);

        return ground;
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose");

        GL4 gl4 = drawable.getGL().getGL4();

        System.exit(0);
    }

    private boolean requireExtension(GL4 gl4, String ext) {
        return requireExtension(gl4, ext, true);
    }

    private boolean requireExtension(GL4 gl4, String ext, boolean exitOnFailure) {
        boolean found = false;
        int[] extensionCount = {0};
        gl4.glGetIntegerv(GL_NUM_EXTENSIONS, extensionCount, 0);
        for (int i = 0; i < extensionCount[0]; i++) {
            if (gl4.glGetStringi(GL_EXTENSIONS, i).equals(ext)) {
                return true;
            }
        }
        if (!found) {
            if (exitOnFailure) {
                System.err.println("The current system does not appear to support the extension "
                        + ext + ", which is required by the sample. This is likely because "
                        + "the system's GPU or driver does not support the extension. "
                        + "Please see the sample's source code for details");
                quit();
            }
        }
        return found;
    }

    private void quit() {
        BindlessApp.animator.stop();
        BindlessApp.glWindow.destroy();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            quit();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
