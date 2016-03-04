/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp.v30;

import com.jogamp.common.util.Ringbuffer;
import com.jogamp.newt.event.KeyEvent;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import com.jogamp.opengl.util.GLBuffers;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private ByteBuffer transformPointer;
    public static IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);

    // uniform buffer object (UBO) for mesh param data
    private PerMesh[] perMesh;

    //bindless texture handle
    private IntBuffer textureName;
    private int currentFrame = 0;
    private float currentTime = 0.0f;

    // UI stuff
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;

    // Timing related stuff
    private float t = 0.0f;
    private float minimumFrameDeltaTime = 1e6f;

    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
    private FloatBuffer clearDepth = GLBuffers.newDirectFloatBuffer(new float[]{1.0f});

    private RingBuffer transformRing;

    public class Buffer {

        public static final int TRANSFORM = 0;
        public static final int CONSTANT = 1;
        public static final int MESH_ID = 2;
        public static final int MAX = 4;
    }

    public BindlessApp(int width, int height) {
        super("BindlessApp");

        transformer.setTranslationVec(new Vec3(0.0f, 0.0f, -4.0f));
    }

    @Override
    public void initRendering(GL4 gl4) {

        // Create our pixel and vertex shader
        shader = NvGLSLProgram.createFromFiles(gl4, "src/gl4_kepler/bindlessApp/v30/shaders", "v30");
        // Set the initial view
        transformer.setRotationVec(new Vec3((float) Math.toRadians(30.0f), (float) Math.toRadians(30.0f), 0.0f));

        if (Mesh.useVertexArray && !Mesh.setVertexFormatOnEveryDrawCall) {
            gl4.glGenVertexArrays(1, vertexArrayName);
        }

        // Create the meshes
        meshes = new Mesh[1 + SQRT_BUILDING_COUNT * SQRT_BUILDING_COUNT];
        perMesh = new PerMesh[meshes.length];
        for (int i = 0; i < perMesh.length; i++) {
            perMesh[i] = new PerMesh();
        }

        initBuffers(gl4);

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

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        transformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                0,
                transformRing.size,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    private void initBuffers(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        {
            int uniformBlockSize = Math.max(Transform.SIZE, uniformBufferOffset.get(0));
            transformRing = new RingBuffer(3, uniformBlockSize);
            gl4.glBufferStorage(GL_UNIFORM_BUFFER,
                    transformRing.size,
                    null,
                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        }
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.CONSTANT));
        {
            int uniformBlockSize = Math.max(Integer.BYTES, uniformBufferOffset.get(0));
            ByteBuffer constantBuffer = GLBuffers.newDirectByteBuffer(Integer.BYTES);
            constantBuffer.asIntBuffer().put(renderTextures ? 1 : 0);
            gl4.glBufferStorage(GL_UNIFORM_BUFFER,
                    uniformBlockSize,
                    constantBuffer,
                    0);
            BufferUtils.destroyDirectBuffer(constantBuffer);
        }
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.MESH_ID));
        {
            ByteBuffer meshIdBuffer = GLBuffers.newDirectByteBuffer(meshes.length * Integer.BYTES);
            for (int i = 0; i < meshes.length; i++) {
                // 0xffff is for ground
                meshIdBuffer.putInt(i * Integer.BYTES, i == 0 ? 0xffff : (i - 1));
            }
            gl4.glBufferStorage(GL_ARRAY_BUFFER,
                    meshes.length * Integer.BYTES,
                    meshIdBuffer,
                    0);
            BufferUtils.destroyDirectBuffer(meshIdBuffer);
        }
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(uniformBufferOffset);
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

                gl4.glTextureStorage2D(textureName.get(0), texture.levels(), glFormat.internal.value,
                        texture.dimensions()[0], texture.dimensions()[1]);

                for (int level = 0; level < texture.levels(); level++) {

                    gl4.glTextureSubImage2D(textureName.get(0), level,
                            0, 0,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            glFormat.external.value, glFormat.type.value,
                            texture.data(level));
                }
            } catch (IOException ex) {
                Logger.getLogger(BindlessApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void draw(GL4 gl4) {

        gl4.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl4.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        gl4.glEnable(GL_DEPTH_TEST);

        // Enable the vertex and pixel shader
        shader.enable(gl4);

        // Set the transformation matrices up
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        {
            Mat4 mvMat = transformer.getModelViewMat();
            Mat4 mvpMat = projectionMat.mul_(mvMat);

            //Wait until the gpu is no longer using the buffer
            transformRing.wait(gl4);

            float[] mat = mvpMat.toFa_();
            for (int i = 0; i < 16; i++) {
                transformPointer.putFloat(transformRing.writeId * transformRing.sectorSize + i * Float.BYTES, mat[i]);
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

                transformPointer.putFloat(transformRing.writeId * transformRing.sectorSize + Mat4.SIZE, t);
            }
        }
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glBindBufferRange(GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM,
                bufferName.get(Buffer.TRANSFORM),
                transformRing.bindId * transformRing.sectorSize,
                transformRing.size);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CONSTANT, bufferName.get(Buffer.CONSTANT));

        if (renderTextures) {
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(currentFrame));
            gl4.glUniform1i(shader.getUniformLocation(gl4, "texture_"), 0);
        }

        if (!usePerMeshUniforms) {
//            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.PER_MESH));
//            {
//                perMeshPointer.asFloatBuffer().put(perMesh[0].toFa());
//
//                gl4.glBufferSubData(GL_UNIFORM_BUFFER, 0, PerMesh.SIZE, perMeshPointer);
//            }
//            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        if (Mesh.useVertexArray && !Mesh.setVertexFormatOnEveryDrawCall) {
            gl4.glBindVertexArray(vertexArrayName.get(0));
        }

        if (!Mesh.setVertexFormatOnEveryDrawCall) {
            Mesh.renderPrep_(gl4);
        }

        // Render all of the meshes
        for (int i = 0; i < meshes.length; i++) {

            // If enabled, update the per mesh uniforms for each mesh rendered
            if (usePerMeshUniforms) {
            }

            if (Mesh.setVertexFormatOnEveryDrawCall) {
                meshes[i].renderPrep(gl4);
            } else {
                meshes[i].setVertexBuffers(gl4);
            }

            meshes[i].render(gl4, i);

            if (Mesh.setVertexFormatOnEveryDrawCall) {
                Mesh.renderFinish(gl4);
            }
        }
        //Place a fence which will be removed when the draw command has finished and update indices
        transformRing.lockAndUpdate(gl4);

        if (!Mesh.setVertexFormatOnEveryDrawCall) {
            Mesh.renderFinish(gl4);
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
    }

    @Override
    public void shutdownRendering(GL4 gl4) {

//        gl4.glDeleteProgram(shader.programName);
//        if (renderTextures) {
//            gl4.glDeleteTextures(TEXTURE_FRAME_COUNT, textureName);
//        }
//        for (Mesh meshe : meshes) {
//            meshe.dispose(gl4);
//        }
//        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
//        if (Mesh.useVertexArray && !Mesh.setVertexFormatOnEveryDrawCall) {
//            gl4.glDeleteVertexArrays(1, vertexArrayName);
//        }
//        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
//        gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
//        
//        if (renderTextures) {
//            BufferUtils.destroyDirectBuffer(textureName);
//        }
//        BufferUtils.destroyDirectBuffer(bufferName);
//        BufferUtils.destroyDirectBuffer(clearColor);
//        BufferUtils.destroyDirectBuffer(clearDepth);
//        BufferUtils.destroyDirectBuffer(perMeshPointer);
//        BufferUtils.destroyDirectBuffer(transformPointer);
//        BufferUtils.destroyDirectBuffer(vertexArrayName);
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
