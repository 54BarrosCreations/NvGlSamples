/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp.v0;

import com.jogamp.newt.event.KeyEvent;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Gl;
import jgli.Load;
import nvAppBase.BufferUtils;
import nvAppBase.NvSampleApp;
import nvAppBase.ProgramEntry;

/**
 *
 * @author GBarbieri
 */
public class BindlessApp extends NvSampleApp {

    private final int SQRT_BUILDING_COUNT = 200;
    private final int TEXTURE_FRAME_COUNT = 180;
    private final float ANIMATION_DURATION = 5f;

    // Simple collection of meshes to render
    private Mesh[] meshes;

    // Shader stuff
    private NvGLSLProgram shader;
    // uniform buffer object (UBO) for tranform data
    private Mat4 projectionMat;
    private FloatBuffer mvpMatBuffer = GLBuffers.newDirectFloatBuffer(16);

    // uniform buffer object (UBO) for mesh param data
    private PerMeshUniforms[] perMesh;

    //bindless texture handle
    private IntBuffer textureName;
    private int currentFrame = 0;
    private float currentTime = 0.0f;

    // UI stuff
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = true;
    private boolean queryUniformsOnce = true;
    private int[] unLoc;

    private class Var {

        public static final int MVP = 0;
        public static final int USE_TEX = 1;
        public static final int TEX = 2;
        public static final int R = 3;
        public static final int G = 4;
        public static final int B = 5;
        public static final int A = 6;
        public static final int U = 7;
        public static final int V = 8;
        public static final int MAX = 9;
    }

    // Timing related stuff
    private float t = 0.0f;
    private float minimumFrameDeltaTime = 1e6f;

    public BindlessApp(int width, int height) {
        super("BindlessApp");

        transformer.setTranslationVec(new Vec3(0.0f, 0.0f, -4.0f));
    }

    @Override
    public void initRendering(GL4 gl4) {

        // Create our pixel and vertex shader
        shader = NvGLSLProgram.createFromFiles(gl4, "src/gl4_kepler/bindlessApp/v0/shaders", "v0");
        // Set the initial view
        transformer.setRotationVec(new Vec3((float) Math.toRadians(30.0f), (float) Math.toRadians(30.0f), 0.0f));

        // Create the meshes
        meshes = new Mesh[1 + SQRT_BUILDING_COUNT * SQRT_BUILDING_COUNT];
        perMesh = new PerMeshUniforms[meshes.length];
        for (int i = 0; i < perMesh.length; i++) {
            perMesh[i] = new PerMeshUniforms();
        }

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

        if (renderTextures) {
            // Initialize Textures
            initTextures(gl4);
        }

        // Initialize the per mesh Uniforms
        updatePerMeshUniforms(0.0f);

        checkError(gl4, "BindlessApp.initRendering()");

        if (queryUniformsOnce) {

            unLoc = new int[Var.MAX];

            shader.enable(gl4);
            {
                unLoc[Var.MVP] = shader.getUniformLocation(gl4, "modelViewProjection");
                unLoc[Var.USE_TEX] = shader.getUniformLocation(gl4, "renderTexture");
                unLoc[Var.TEX] = shader.getUniformLocation(gl4, "texture_");
                unLoc[Var.R] = shader.getUniformLocation(gl4, "r");
                unLoc[Var.G] = shader.getUniformLocation(gl4, "g");
                unLoc[Var.B] = shader.getUniformLocation(gl4, "b");
                unLoc[Var.A] = shader.getUniformLocation(gl4, "a");
                unLoc[Var.U] = shader.getUniformLocation(gl4, "u");
                unLoc[Var.V] = shader.getUniformLocation(gl4, "v");
            }
            shader.disable(gl4);
        }
    }

    /**
     * Computes per mesh uniforms based on t.
     */
    private void updatePerMeshUniforms(float t) {

        // If we're using per mesh uniforms, compute the values for the uniforms for all of the meshes and
        // give the data to the GPU.
        if (usePerMeshUniforms) {

            // Update uniforms for the "ground" mesh
            perMesh[0].r = 1.0f;
            perMesh[0].g = 1.0f;
            perMesh[0].b = 1.0f;
            perMesh[0].a = 0.0f;

            // Compute the per mesh uniforms for all of the "building" meshes
            int index = 1;
            for (int i = 0; i < SQRT_BUILDING_COUNT; i++) {
                for (int j = 0; j < SQRT_BUILDING_COUNT; j++, index++) {
                    float x, z, radius;

                    x = (float) i / SQRT_BUILDING_COUNT - 0.5f;
                    z = (float) j / SQRT_BUILDING_COUNT - 0.5f;
                    radius = (float) Math.sqrt((x * x) + (z * z));

                    perMesh[index].r = (float) Math.sin(10.0f * radius + t);
                    perMesh[index].g = (float) Math.cos(10.0f * radius + t);
                    perMesh[index].b = radius;
                    perMesh[index].a = 0.0f;
                    perMesh[index].u = (float) j / SQRT_BUILDING_COUNT;
                    perMesh[index].v = 1 - (float) i / SQRT_BUILDING_COUNT;
                }
            }
        } else {
            // All meshes will use these uniforms
            perMesh[0].r = (float) Math.sin(t);
            perMesh[0].g = (float) Math.cos(t);
            perMesh[0].b = 1.0f;
            perMesh[0].a = 0.0f;
        }
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
        for (int i = 0; i < 24; i += 4) {

            indices[i / 4 * 6 + 0] = (short) (0 + i);
            indices[i / 4 * 6 + 1] = (short) (1 + i);
            indices[i / 4 * 6 + 2] = (short) (2 + i);

            indices[i / 4 * 6 + 3] = (short) (0 + i);
            indices[i / 4 * 6 + 4] = (short) (2 + i);
            indices[i / 4 * 6 + 5] = (short) (3 + i);
        }

        Mesh building = new Mesh();
        building.init(gl4, vertices, indices);

        return building;
    }

    /**
     * Generates a random color.
     */
    private float randomColor() {
        return (float) (1 - Math.random());
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
        ground.init(gl4, vertices, indices);

        return ground;
    }

    private void initTextures(GL4 gl4) {

        textureName = GLBuffers.newDirectIntBuffer(TEXTURE_FRAME_COUNT);

        gl4.glGenTextures(TEXTURE_FRAME_COUNT, textureName);

        for (int i = 0; i < TEXTURE_FRAME_COUNT; i++) {

            try {
                jgli.Texture texture = Load.load("/gl4_kepler/bindlessApp/textures/NV" + i + ".dds");
                
                gl4.glActiveTexture(GL_TEXTURE0);
                gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(i));
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
                gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
                gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
                gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
                gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);

                jgli.Gl.Format glFormat = Gl.translate(texture.format());

                for (int level = 0; level < texture.levels(); level++) {

                    gl4.glTexImage2D(GL_TEXTURE_2D, level, glFormat.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1], 0,
                            glFormat.external.value, glFormat.type.value, texture.data(level));
                }
            } catch (IOException ex) {
                Logger.getLogger(BindlessApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void draw(GL4 gl4) {

        gl4.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl4.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl4.glEnable(GL_DEPTH_TEST);

        // Enable the vertex and pixel shader
        shader.enable(gl4);

        // Set the transformation matrices up
        {
            Mat4 mvMat = transformer.getModelViewMat();
            Mat4 mvpMat = projectionMat.mul_(mvMat);
            mvpMatBuffer.put(mvpMat.toFa_()).rewind();
            gl4.glUniformMatrix4fv(
                    queryUniformsOnce ? unLoc[Var.MVP] : shader.getUniformLocation(gl4, "modelViewProjection"),
                    1, false, mvpMatBuffer);
        }

        gl4.glUniform1i(
                queryUniformsOnce ? unLoc[Var.USE_TEX] : shader.getUniformLocation(gl4, "useTexture"),
                renderTextures ? 1 : 0);
        if (renderTextures) {
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(currentFrame));
            gl4.glUniform1i(
                    queryUniformsOnce ? unLoc[Var.TEX] : shader.getUniformLocation(gl4, "texture_"),
                    0);
        }

        // If we are going to update the uniforms every frame, do it now
        if (updateUniformsEveryFrame) {

            float deltaTime;
            float dt;

            deltaTime = getFrameDeltaTime();

            if (deltaTime < minimumFrameDeltaTime) {
                minimumFrameDeltaTime = deltaTime;
            }

            dt = Math.min(0.00005f / minimumFrameDeltaTime, .01f);
            t += dt * Mesh.drawCallsPerState;

            updatePerMeshUniforms(t);
        }

        if (!usePerMeshUniforms) {

            gl4.glUniform1f(
                    queryUniformsOnce ? unLoc[Var.R] : shader.getUniformLocation(gl4, "r"), 
                    perMesh[0].r);
            gl4.glUniform1f(
                    queryUniformsOnce ? unLoc[Var.G] : shader.getUniformLocation(gl4, "g"), 
                    perMesh[0].g);
            gl4.glUniform1f(
                    queryUniformsOnce ? unLoc[Var.B] : shader.getUniformLocation(gl4, "b"), 
                    perMesh[0].b);
            gl4.glUniform1f(
                    queryUniformsOnce ? unLoc[Var.A] : shader.getUniformLocation(gl4, "a"), 
                    perMesh[0].a);
            gl4.glUniform1f(
                    queryUniformsOnce ? unLoc[Var.U] : shader.getUniformLocation(gl4, "u"), 
                    perMesh[0].u);
            gl4.glUniform1f(
                    queryUniformsOnce ? unLoc[Var.V] : shader.getUniformLocation(gl4, "v"), 
                    perMesh[0].v);
        }

        // Render all of the meshes
        for (int i = 0; i < meshes.length; i++) {

            // If enabled, update the per mesh uniforms for each mesh rendered
            if (usePerMeshUniforms) {

                gl4.glUniform1f(shader.getUniformLocation(gl4, "r"), perMesh[i].r);
                gl4.glUniform1f(shader.getUniformLocation(gl4, "g"), perMesh[i].g);
                gl4.glUniform1f(shader.getUniformLocation(gl4, "b"), perMesh[i].b);
                gl4.glUniform1f(shader.getUniformLocation(gl4, "a"), perMesh[i].a);
                gl4.glUniform1f(shader.getUniformLocation(gl4, "u"), perMesh[i].u);
                gl4.glUniform1f(shader.getUniformLocation(gl4, "v"), perMesh[i].v);
            }

            meshes[i].renderPrep(gl4);
            {
                meshes[i].render(gl4);
            }
            meshes[i].renderFinish(gl4);
        }

        // Disable the vertex and pixel shader
        shader.disable(gl4);

        currentTime += getFrameDeltaTime();
        if (currentTime > ANIMATION_DURATION) {
            currentTime = 0.0f;
        }
        currentFrame = (int) (180.0f * currentTime / ANIMATION_DURATION);
    }

    @Override
    public void reshape(GL4 gl4, int x, int y, int width, int height) {

        gl4.glViewport(x, y, width, height);
        projectionMat = glm.perspective_(45f * 2f * (float) Math.PI / 360f, (float) width / height, .1f, 10f);

        checkError(gl4, "BindlessApp.reshape");
    }

    @Override
    public void shutdownRendering(GL4 gl4) {

        gl4.glDeleteProgram(shader.programName);
        gl4.glDeleteTextures(TEXTURE_FRAME_COUNT, textureName);
        for (Mesh meshe : meshes) {
            meshe.dispose(gl4);
        }
        BufferUtils.destroyDirectBuffer(textureName);
        BufferUtils.destroyDirectBuffer(mvpMatBuffer);
    }

    private boolean requireExtension(GL4 gl4, String ext) {
        return requireExtension(gl4, ext, true);
    }

    private boolean requireExtension(GL4 gl4, String ext, boolean exitOnFailure) {
        if (gl4.isExtensionAvailable(ext)) {
            return true;
        }
        if (exitOnFailure) {
            System.err.println("The current system does not appear to support the extension " + ext + ", which is "
                    + "required by the sample. This is likely because the system's GPU or driver does not support the "
                    + "extension. Please see the sample's source code for details");
            quit();
        }
        return false;
    }

    private void quit() {
        ProgramEntry.animator.stop();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO add key list
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            quit();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
