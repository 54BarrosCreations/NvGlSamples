/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp;

import nvAppBase.NvGLSLProgram;
import com.jogamp.newt.event.KeyEvent;
import static com.jogamp.opengl.GL.GL_BUFFER_SIZE;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_RESULT;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_TIME_ELAPSED;
import static com.jogamp.opengl.GL2ES3.GL_NUM_EXTENSIONS;
import static com.jogamp.opengl.GL2ES3.GL_READ_ONLY;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2GL3.GL_BUFFER_GPU_ADDRESS_NV;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import jglm.Jglm;
import jglm.Mat4;
import jglm.Vec2;
import jglm.Vec3;
import nvAppBase.NvSampleApp;
import nvAppBase.ProgramEntry;

/**
 *
 * @author GBarbieri
 */
public class BindlessApp extends NvSampleApp {

    private final int SQRT_BUILDING_COUNT = 100;

    // Simple collection of meshes to render
    private Mesh[] meshes;

    // Shader stuff
    private NvGLSLProgram shader;
    private int bindlessPerMeshUniformsPtrAttribLocation;

    // uniform buffer object (UBO) for tranform data
    private int[] transformUniformsName = {0};
    private TransformUniforms transformUniformsData;
    private Mat4 projectionMatrix;
    private ByteBuffer transformBuffer;

    // uniform buffer object (UBO) for mesh param data
    private int[] perMeshUniformsName = {0};
    private PerMeshUniforms[] perMeshUniformsData;
    private long[] perMeshUniformsGPUPtr = {0};
    private FloatBuffer perMeshUniformsBuffer;

    //bindless texture handle
    private long[] textureHandles;
    private int textureIds;
    private int numTextures;
    private boolean useBindlessTextures = false;
    private int currentFrame = 0;
    private float currentTime = 0.0f;

    // UI stuff
//    NvUIValueText*                m_drawCallsPerSecondText;
    private boolean useBindlessUniforms = true;
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;

    // Timing related stuff
    private float t = 0.0f;
    private float minimumFrameDeltaTime = 1e6f;

    public BindlessApp(int width, int height) {
        super("BindlessApp");
        // TODO
        transformer.setTranslationVec(new Vec3(0.0f, 0.0f, -4.0f));

        transformUniformsData = new TransformUniforms();
    }

    @Override
    public void initRendering(GL4 gl4) {

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

        bindlessPerMeshUniformsPtrAttribLocation = shader.getAttribLocation(gl4, "bindlessPerMeshUniformsPtr", true);
        System.out.println("bindlessPerMeshUniformsPtrAttribLocation " + bindlessPerMeshUniformsPtrAttribLocation);

        // Set the initial view
        transformer.setRotationVec(new Vec3((float) Math.toRadians(30.0f), (float) Math.toRadians(30.0f), 0.0f));

        // Create the meshes
        meshes = new Mesh[1 + SQRT_BUILDING_COUNT * SQRT_BUILDING_COUNT];
        perMeshUniformsData = new PerMeshUniforms[meshes.length];
        for (int i = 0; i < perMeshUniformsData.length; i++) {
            perMeshUniformsData[i] = new PerMeshUniforms();
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
        /**
         * TODO.
         */
        // Initialize Bindless Textures
//	InitBindlessTextures();

        // create Uniform Buffer Object (UBO) for transform data and initialize 
        gl4.glCreateBuffers(1, transformUniformsName, 0);
        gl4.glNamedBufferData(transformUniformsName[0], TransformUniforms.SIZEOF, null, GL_STREAM_DRAW);

        // create Uniform Buffer Object (UBO) for param data, initialize and allocate space
        gl4.glCreateBuffers(1, perMeshUniformsName, 0);
        gl4.glNamedBufferData(perMeshUniformsName[0], perMeshUniformsData.length * PerMeshUniforms.SIZEOF,
                null, GL_STREAM_DRAW);

        transformBuffer = GLBuffers.newDirectByteBuffer(TransformUniforms.SIZEOF);
        perMeshUniformsBuffer = GLBuffers.newDirectFloatBuffer(
                perMeshUniformsData.length * PerMeshUniforms.SIZEOF / Float.BYTES);
        
        // Initialize the per mesh Uniforms
        updatePerMeshUniforms(gl4, 0.0f);

        gl4.glGenVertexArrays(1, Mesh.vao, 0);
        gl4.glBindVertexArray(Mesh.vao[0]);
        
        gl4.glGenQueries(1, queryName, 0);

        
        
        checkError(gl4, "BindlessApp.initRendering()");
    }

    int[] queryName = {0};
    int[] time = {0};

    /**
     * Computes per mesh uniforms based on t and sends the uniforms to the GPU.
     */
    private void updatePerMeshUniforms(GL4 gl4, float t) {

        // If we're using per mesh uniforms, compute the values for the uniforms for all of the meshes and
        // give the data to the GPU.
        if (usePerMeshUniforms) {

            // Update uniforms for the "ground" mesh
            perMeshUniformsData[0].r = 1.0f;
            perMeshUniformsData[0].g = 1.0f;
            perMeshUniformsData[0].b = 1.0f;
            perMeshUniformsData[0].a = 0.0f;

            // Compute the per mesh uniforms for all of the "building" meshes
            int index = 1;
            for (int i = 0; i < SQRT_BUILDING_COUNT; i++) {
                for (int j = 0; j < SQRT_BUILDING_COUNT; j++, index++) {
                    float x, z, radius;

                    x = (float) i / SQRT_BUILDING_COUNT - 0.5f;
                    z = (float) j / SQRT_BUILDING_COUNT - 0.5f;
                    radius = (float) Math.sqrt((x * x) + (z * z));

                    perMeshUniformsData[index].r = (float) Math.sin(10.0f * radius + t);
                    perMeshUniformsData[index].g = (float) Math.cos(10.0f * radius + t);
                    perMeshUniformsData[index].b = radius;
                    perMeshUniformsData[index].a = 0.0f;
                    perMeshUniformsData[index].u = (float) j / SQRT_BUILDING_COUNT;
                    perMeshUniformsData[index].v = (float) i / SQRT_BUILDING_COUNT;
                }
            }
            // Give the uniform data to the GPU
            for (PerMeshUniforms meshUniforms : perMeshUniformsData) {
                perMeshUniformsBuffer.put(meshUniforms.toFloatArray());
            }
            gl4.glNamedBufferData(perMeshUniformsName[0], perMeshUniformsData.length * PerMeshUniforms.SIZEOF,
                    perMeshUniformsBuffer.rewind(), GL_STREAM_DRAW);
        } else {
            // All meshes will use these uniforms
            perMeshUniformsData[0].r = (float) Math.sin(t);
            perMeshUniformsData[0].g = (float) Math.cos(t);
            perMeshUniformsData[0].b = 1.0f;
            perMeshUniformsData[0].a = 0.0f;

            // Give the uniform data to the GPU
            perMeshUniformsBuffer.put(perMeshUniformsData[0].toFloatArray());
            gl4.glNamedBufferSubData(perMeshUniformsName[0], 0, perMeshUniformsBuffer.capacity() * Float.BYTES,
                    perMeshUniformsBuffer.rewind());
        }

        if (useBindlessUniforms) {
            // *** INTERESTING ***
            // Get the GPU pointer for the per mesh uniform buffer and make the buffer resident on the GPU
            // For bindless uniforms, this GPU pointer will later be passed to the vertex shader via a
            // vertex attribute. The vertex shader will then directly use the GPU pointer to access the
            // uniform data.
            int[] perMeshUniformsDataSize = {0};
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, perMeshUniformsName[0]);
            gl4.glGetBufferParameterui64vNV(GL_UNIFORM_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, perMeshUniformsGPUPtr, 0);
            gl4.glGetBufferParameteriv(GL_UNIFORM_BUFFER, GL_BUFFER_SIZE, perMeshUniformsDataSize, 0);
            gl4.glMakeBufferResidentNV(GL_UNIFORM_BUFFER, GL_READ_ONLY);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
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
        building.update(gl4, vertices, indices);

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
        ground.update(gl4, vertices, indices);

        return ground;
    }

    @Override
    public void draw(GL4 gl4) {

        Mat4 modelviewMatrix;

        gl4.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl4.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl4.glEnable(GL_DEPTH_TEST);

        // Enable the vertex and pixel shader
        shader.enable(gl4);

        if (useBindlessTextures) {
            int samplersLocation = shader.getUniformLocation(gl4, "samplers");
            gl4.glUniformHandleui64vARB(samplersLocation, numTextures, textureHandles, 0);

        }
        int bindlessTexture = shader.getUniformLocation(gl4, "useBindless");
        gl4.glUniform1i(bindlessTexture, useBindlessTextures ? 1 : 0);

        int currentTexture = shader.getUniformLocation(gl4, "currentFrame");
        gl4.glUniform1i(currentTexture, currentFrame);

        // Set up the transformation matices up
        modelviewMatrix = transformer.getModelViewMat();
        transformUniformsData.modelView = modelviewMatrix;
        transformUniformsData.modelViewProjection = projectionMatrix.mult(modelviewMatrix);
        transformUniformsData.useBindlessUniforms = useBindlessUniforms ? 1 : 0;
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 2, transformUniformsName[0]);
        transformBuffer.asFloatBuffer().put(transformUniformsData.modelView.toFloatArray())
                .put(transformUniformsData.modelViewProjection.toFloatArray());
        transformBuffer.putInt(TransformUniforms.USEBINDLESSUNIFORM_OFFSET, transformUniformsData.useBindlessUniforms);
        gl4.glNamedBufferSubData(transformUniformsName[0], 0, TransformUniforms.SIZEOF, transformBuffer.rewind());

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

            updatePerMeshUniforms(gl4, t);
        }

        // Set up default per mesh uniforms. These may be changed on a per mesh basis in the rendering loop below 
        if (useBindlessUniforms) {
            // *** INTERESTING ***
            // Pass a GPU pointer to the vertex shader for the per mesh uniform data via a vertex attribute
            gl4.glVertexAttribI2i(bindlessPerMeshUniformsPtrAttribLocation,
                    (int) (perMeshUniformsGPUPtr[0] & 0xFFFFFFFF),
                    (int) ((perMeshUniformsGPUPtr[0] >> 32) & 0xFFFFFFFF));
        } else {
            gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 3, perMeshUniformsName[0]);
            perMeshUniformsBuffer.put(perMeshUniformsData[0].toFloatArray());
            gl4.glNamedBufferSubData(perMeshUniformsName[0], 0, PerMeshUniforms.SIZEOF, perMeshUniformsBuffer.rewind());
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
                    perMeshUniformsGPUPtr_i_th = perMeshUniformsGPUPtr[0] + PerMeshUniforms.SIZEOF * i;
                    // Pass a GPU pointer to the vertex shader for the per mesh uniform data via a vertex attribute
                    gl4.glVertexAttribI2i(bindlessPerMeshUniformsPtrAttribLocation,
                            (int) (perMeshUniformsGPUPtr_i_th & 0xFFFFFFFF),
                            (int) ((perMeshUniformsGPUPtr_i_th >> 32) & 0xFFFFFFFF));

                } else {

                    gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 3, perMeshUniformsName[0]);
                    perMeshUniformsBuffer.put(perMeshUniformsData[i].toFloatArray());
                    gl4.glNamedBufferSubData(perMeshUniformsName[0], 0, PerMeshUniforms.SIZEOF,
                            perMeshUniformsBuffer.rewind());
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
//            gl4.glBeginQuery(GL_TIME_ELAPSED, queryName[0]);
            meshes[i].render(gl4);
//            gl4.glEndQuery(GL_TIME_ELAPSED);
            // Get the count of samples. 
            // If the result of the query isn't here yet, we wait here...
//            gl4.glGetQueryObjectuiv(queryName[0], GL_QUERY_RESULT, time, 0);
//            System.out.println("Time: " + (time[0] / 1000.f / 1000.f) + " ms");

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
        projectionMatrix = Jglm.perspective(45f, (float) width / height, .1f, 10f);

        checkError(gl4, "BindlessApp.reshape");
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
        // TODO
//        System.exit(0)
        ProgramEntry.animator.stop();
        ProgramEntry.glWindow.destroy();
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
