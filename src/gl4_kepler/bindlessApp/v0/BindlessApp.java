/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp.v0;

import gl4_kepler.bindlessApp.*;
import nvAppBase.NvGLSLProgram;
import com.jogamp.newt.event.KeyEvent;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.TextureIO;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import nvAppBase.NvImage;
import nvAppBase.NvSampleApp;
import nvAppBase.ProgramEntry;

/**
 *
 * @author GBarbieri
 */
public class BindlessApp extends NvSampleApp {

    private final int SQRT_BUILDING_COUNT = 100;
    private final int TEXTURE_FRAME_COUNT = 180;

    // Simple collection of meshes to render
    private Mesh[] meshes;

    // Shader stuff
    private NvGLSLProgram shader;

    // uniform buffer object (UBO) for tranform data
    private IntBuffer transformName = GLBuffers.newDirectIntBuffer(1);
    private Transform transform;
    private Mat4 projectionMatrix;
    private ByteBuffer transformBuffer;

    // uniform buffer object (UBO) for mesh param data
    private IntBuffer perMeshName = GLBuffers.newDirectIntBuffer(1);
    private PerMeshUniforms[] perMesh;
    private FloatBuffer perMeshBuffer;

    //bindless texture handle
    private int[] textureName;
    private int numTextures;
    private int currentTexture = 0;
    private float currentTime = 0.0f;

    // UI stuff
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = true;

    // Timing related stuff
    private float t = 0.0f;
    private float minimumFrameDeltaTime = 1e6f;

    // Allocation
    private boolean usePools = true;

    public BindlessApp(int width, int height) {
        super("BindlessApp");

        transformer.setTranslationVec(new Vec3(0.0f, 0.0f, -4.0f));

        transform = new Transform();
    }

    @Override
    public void initRendering(GL4 gl4) {

        // Create our pixel and vertex shader
        shader = NvGLSLProgram.createFromFiles(gl4, "src/gl4_kepler/bindlessApp/shaders", "bindless");

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

            gl4.glUniform1i(shader.getUniformLocation(gl4, "renderTextures"), renderTextures ? 1 : 0);

            gl4.glUniform1i(shader.getUniformLocation(gl4, "currentTexture"), currentTexture);
        }

        // create Uniform Buffer Object (UBO) for transform data and initialize 
        gl4.glGenBuffers(1, transformName);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, transformName.get(0));
        {
            gl4.glBufferData(GL_UNIFORM_BUFFER, Transform.SIZE, null, GL_DYNAMIC_DRAW);
        }
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        // create Uniform Buffer Object (UBO) for param data, initialize and allocate space
        gl4.glGenBuffers(1, perMeshName);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, transformName.get(0));
        gl4.glBufferData(perMeshName.get(0), PerMeshUniforms.SIZE, null, GL_DYNAMIC_DRAW);

        if (usePools) {
            transformBuffer = GLBuffers.newDirectByteBuffer(Transform.SIZE);
            perMeshBuffer = GLBuffers.newDirectFloatBuffer(PerMeshUniforms.SIZE / Float.BYTES);
        }

        // Initialize the per mesh Uniforms
        updatePerMeshUniforms(0.0f);

        checkError(gl4, "BindlessApp.initRendering()");
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
                    perMesh[index].v = (float) i / SQRT_BUILDING_COUNT;
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

        textureName = new int[TEXTURE_FRAME_COUNT];
        numTextures = TEXTURE_FRAME_COUNT;

        for (int i = 0; i < TEXTURE_FRAME_COUNT; i++) {

            try {
                textureName[i] = NvImage.uploadTextureFromDDSFile(gl4, "/gl4_kepler/bindlessApp/textures/NV"
                        + i + "." + TextureIO.DDS);
            } catch (IOException ex) {
                Logger.getLogger(BindlessApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            gl4.glBindTexture(GL4.GL_TEXTURE_2D, textureName[i]);
            gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
            gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
            gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
            gl4.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);
        }
    }

    @Override
    public void draw(GL4 gl4) {

        Mat4 modelviewMatrix;

        gl4.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl4.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl4.glEnable(GL_DEPTH_TEST);

        // Enable the vertex and pixel shader
        shader.enable(gl4);

        // Set the transformation matrices up
        modelviewMatrix = transformer.getModelViewMat();
        transform.modelView = modelviewMatrix;
        transform.modelViewProjection = projectionMatrix.mul_(modelviewMatrix);
        
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 2, transformName.get(0));
        if (!usePools) {
            transformBuffer = GLBuffers.newDirectByteBuffer(Transform.SIZE);
        }
        transformBuffer.asFloatBuffer()
                .put(transform.modelView.toFa_())
                .put(transform.modelViewProjection.toFa_());
        transformBuffer.putInt(Transform.USEBINDLESSUNIFORM_OFFSET, transform.useBindlessUniforms);
        gl4.glNamedBufferSubData(transformName.get(0), 0, Transform.SIZE, transformBuffer);

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

        // If all of the meshes are sharing the same vertex format, we can just set the vertex format once
        if (!Mesh.setVertexFormatOnEveryDrawCall) {
            Mesh.renderPrep(gl4);
        }

        // Render all of the meshes
        for (int i = 0; i < meshes.length; i++) {

            // If enabled, update the per mesh uniforms for each mesh rendered
            if (usePerMeshUniforms) {

                if (useBindlessUniforms) {

                    long perMeshUniformsGPUPtr_i_th;

                    // *** INTERESTING ***
                    // Compute a GPU pointer for the per mesh uniforms for this mesh
                    perMeshUniformsGPUPtr_i_th = perMeshUniformsGPUPtr[0] + PerMeshUniforms.SIZE * i;
                    // Pass a GPU pointer to the vertex shader for the per mesh uniform data via a vertex attribute
                    gl4.glVertexAttribI2i(bindlessPerMeshUniformsPtrAttribLocation,
                            (int) (perMeshUniformsGPUPtr_i_th & 0xFFFFFFFF),
                            (int) ((perMeshUniformsGPUPtr_i_th >> 32) & 0xFFFFFFFF));

                } else {

                    gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 3, perMeshName.get(0));
                    if (!usePools) {
                        perMeshBuffer = GLBuffers.newDirectFloatBuffer(PerMeshUniforms.SIZE / Float.BYTES);
                    }
                    perMeshBuffer.put(perMesh[i].toFloatArray()).rewind();
                    gl4.glNamedBufferSubData(perMeshName.get(0), 0, PerMeshUniforms.SIZE, perMeshBuffer);
                }
            }

            // If we're not sharing vertex formats between meshes, we have to set the vertex format everytime it changes.
            if (Mesh.setVertexFormatOnEveryDrawCall) {
                Mesh.renderPrep(gl4);
            }

            // Now that everything is set up, do the actual rendering
            // The code that selects between rendering with Vertex Array Objects (VAO) and 
            // Vertex Buffer Unified Memory (VBUM) is located in Mesh::render()
            // The code that gets the GPU pointer for use with VBUM rendering is located in Mesh::update()
            // Beginning of the time query
            meshes[i].render(gl4);

            // If we're not sharing vertex formats between meshes, we have to reset the vertex format to a default state after each mesh
            if (Mesh.setVertexFormatOnEveryDrawCall) {
                Mesh.renderFinish(gl4);
            }
        }

        // If we're sharing vertex formats between meshes, we only have to reset vertex format to a default state once
        if (!Mesh.setVertexFormatOnEveryDrawCall) {
            Mesh.renderFinish(gl4);
        }

        // Disable the vertex and pixel shader
        shader.disable(gl4);
    }

    @Override
    public void reshape(GL4 gl4, int x, int y, int width, int height) {

        gl4.glViewport(x, y, width, height);
        projectionMatrix = glm.perspective_(45f, (float) width / height, .1f, 10f);

        checkError(gl4, "BindlessApp.reshape");
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
