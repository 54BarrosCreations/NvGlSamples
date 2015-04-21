/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.util;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import java.util.ArrayList;

/**
 *
 * @author elect
 */
public class Mesh {

    public static int drawCallsPerState = 1;
    public static boolean setVertexFormatOnEveryDrawCall = false;
    public static boolean enableVBUM = false;
    public static boolean useHeavyVertexFormat = false;
    private int[] vertexBuffer;         // vertex buffer object for vertices
    private int[] indexBuffer;          // vertex buffer object for indices
    private long[] vertexBufferGPUPtr;  // GPU pointer to m_vertexBuffer data
    private int[] vertexBufferSize;
    private long[] indexBufferGPUPtr;   // GPU pointer to m_indexBuffer data
    private int[] indexBufferSize;
    private int vertexCount;            // Number of vertices in mesh
    private int indexCount;             // Number of indices in mesh

    public Mesh() {

        vertexBuffer = new int[1];
        indexBuffer = new int[1];

        vertexBufferGPUPtr = new long[1];
        vertexBufferSize = new int[1];

        indexBufferGPUPtr = new long[1];
        indexBufferSize = new int[1];

        indexBufferGPUPtr = new long[1];
    }

    public void update(GL4 gl4, ArrayList<Vertex> vertices, ArrayList<Short> indices) {

        if (vertexBuffer[0] == 0) {

            gl4.glCreateBuffers(1, vertexBuffer, 0);
        }
        if (indexBuffer[0] == 0) {

            gl4.glCreateBuffers(1, indexBuffer, 0);
        }
        // Stick the data for the vertices and indices in their respective buffers
        int vertexLenght = vertices.get(0).toFloatArray().length;
        float[] verticesArray = new float[vertices.size() * vertexLenght];
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            System.arraycopy(vertex.toFloatArray(), 0, verticesArray, i * vertexLenght, vertexLenght);
        }
        short[] indicesArray = new short[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }
        /**
         * Here they dont work, it seems you have to bind the corresponding
         * buffers before or switching glGenBuffer to glCreateBuffer.
         * http://stackoverflow.com/questions/29743881/glnamedbufferdata-fires-gl-invalid-operation
         * https://www.opengl.org/discussion_boards/showthread.php/186134-glNamedBufferData-fires-GL_INVALID_OPERATION
         */
        gl4.glNamedBufferData(vertexBuffer[0], Vertex.size() * vertices.size(),
                GLBuffers.newDirectFloatBuffer(verticesArray), GL4.GL_STATIC_DRAW);
        gl4.glNamedBufferData(indexBuffer[0], GLBuffers.SIZEOF_SHORT * indices.size(),
                GLBuffers.newDirectShortBuffer(indicesArray), GL4.GL_STATIC_DRAW);

        // *** INTERESTING ***
        // get the GPU pointer for the vertex buffer and make the vertex buffer 
        // resident on the GPU
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, vertexBuffer[0]);
        gl4.glGetBufferParameterui64vNV(GL4.GL_ARRAY_BUFFER, GL4.GL_BUFFER_GPU_ADDRESS_NV,
                vertexBufferGPUPtr, 0);
        gl4.glGetBufferParameteriv(GL4.GL_ARRAY_BUFFER, GL4.GL_BUFFER_SIZE,
                vertexBufferSize, 0);
        gl4.glMakeBufferResidentNV(GL4.GL_ARRAY_BUFFER, GL4.GL_READ_ONLY);
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);

        // *** INTERESTING ***
        // get the GPU pointer for the vertex buffer and make the vertex buffer 
        // resident on the GPU
        gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);
        gl4.glGetBufferParameterui64vNV(GL4.GL_ELEMENT_ARRAY_BUFFER,
                GL4.GL_BUFFER_GPU_ADDRESS_NV, indexBufferGPUPtr, 0);
        gl4.glGetBufferParameteriv(GL4.GL_ELEMENT_ARRAY_BUFFER, GL4.GL_BUFFER_SIZE,
                indexBufferSize, 0);
        gl4.glMakeBufferResidentNV(GL4.GL_ELEMENT_ARRAY_BUFFER, GL4.GL_READ_ONLY);
        gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, 0);

        vertexCount = vertices.size();
        indexCount = indices.size();
    }

    /**
     * Sets up the vertex format state.
     *
     * @param gl4
     */
    public static void renderPrep(GL4 gl4) {
        System.out.println("renderPrep");
        if (enableVBUM) {

            /**
             * Specify the vertex format.
             */
            // Position in attribute 0 that is 3 floats
            gl4.glVertexAttribFormatNV(0, 3, GL4.GL_FLOAT, false, Vertex.size());
            // Color in attribute 1 that is 4 unsigned bytes
            gl4.glVertexAttribFormatNV(1, 4, GL4.GL_UNSIGNED_BYTE, true, Vertex.size());

            gl4.glVertexFormatNV(3, GL4.GL_FLOAT, Vertex.size());

            // Enable the relevent attributes
            gl4.glEnableVertexAttribArray(0);
            gl4.glEnableVertexAttribArray(1);

            // Enable a bunch of other attributes if we're using the heavy 
            // vertex format option
            if (useHeavyVertexFormat) {

                gl4.glVertexAttribFormatNV(3, 4, GL4.GL_FLOAT, false, Vertex.size());
                gl4.glVertexAttribFormatNV(4, 4, GL4.GL_FLOAT, false, Vertex.size());
                gl4.glVertexAttribFormatNV(5, 4, GL4.GL_FLOAT, false, Vertex.size());
                gl4.glVertexAttribFormatNV(6, 4, GL4.GL_FLOAT, false, Vertex.size());
                gl4.glVertexAttribFormatNV(7, 4, GL4.GL_FLOAT, false, Vertex.size());

                gl4.glEnableVertexAttribArray(3);
                gl4.glEnableVertexAttribArray(4);
                gl4.glEnableVertexAttribArray(5);
                gl4.glEnableVertexAttribArray(6);
                gl4.glEnableVertexAttribArray(7);
            }
            // Enable Vertex Buffer Unified Memory (VBUM) for the vertex attributes
            gl4.glEnableClientState(GL4.GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);

            // Enable Vertex Buffer Unified Memory (VBUM) for the indices
            gl4.glEnableClientState(GL4.GL_ELEMENT_ARRAY_UNIFIED_NV);

        } else {

            // For Vertex Array Objects (VAO), enable the vertex attributes
            gl4.glEnableVertexArrayAttrib(0, 0);
            gl4.glEnableVertexArrayAttrib(0, 1);

            // Enable a bunch of other attributes if we're using the heavy 
            // vertex format option
            if (useHeavyVertexFormat) {

                gl4.glEnableVertexArrayAttrib(0, 3);
                gl4.glEnableVertexArrayAttrib(0, 4);
                gl4.glEnableVertexArrayAttrib(0, 5);
                gl4.glEnableVertexArrayAttrib(0, 6);
                gl4.glEnableVertexArrayAttrib(0, 7);
            }
        }
    }

    /**
     * Does the actual rendering of the mesh
     *
     * @param gl4
     */
    public void render(GL4 gl4) {
        System.out.println("render");
        if (vertexBuffer[0] == 0) {
            System.out.println("Error, vertexBuffer == 0");
        }
        if (indexBuffer[0] == 0) {
            System.out.println("Error, indexBuffer == 0");
        }

        if (enableVBUM) {
            /**
             * Render using Vertex Buffer Unified Memory (VBUM).
             */
            // *** INTERESTING ***
            // Set up the pointers in GPU memory to the vertex attributes.
            // The GPU pointer to the vertex buffer was stored in Mesh::update() 
            // after the buffer was filled
            gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 0,
                    //            gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ARRAY_ADDRESS_NV, 0,
                    vertexBufferGPUPtr[0] + Vertex.positionOffset,
                    vertexBufferSize[0] - Vertex.positionOffset);
            gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 1,
                    //            gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ARRAY_ADDRESS_NV, 1,
                    vertexBufferGPUPtr[0] + Vertex.colorOffset,
                    vertexBufferSize[0] - Vertex.colorOffset);

            // Set a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {
                gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 3,
                        vertexBufferGPUPtr[0] + Vertex.attrib1Offset,
                        vertexBufferSize[0] - Vertex.attrib1Offset);
                gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 4,
                        vertexBufferGPUPtr[0] + Vertex.attrib2Offset,
                        vertexBufferSize[0] - Vertex.attrib2Offset);
                gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 5,
                        vertexBufferGPUPtr[0] + Vertex.attrib3Offset,
                        vertexBufferSize[0] - Vertex.attrib3Offset);
                gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 6,
                        vertexBufferGPUPtr[0] + Vertex.attrib4Offset,
                        vertexBufferSize[0] - Vertex.attrib4Offset);
                gl4.glBufferAddressRangeNV(GL4.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, 7,
                        vertexBufferGPUPtr[0] + Vertex.attrib5Offset,
                        vertexBufferSize[0] - Vertex.attrib5Offset);
            }
            // *** INTERESTING ***
            // Set up the pointer in GPU memory to the index buffer
            gl4.glBufferAddressRangeNV(GL4.GL_ELEMENT_ARRAY_ADDRESS_NV, 0,
                    indexBufferGPUPtr[0], indexBufferSize[0]);

            // Do the actual drawing
            for (int i = 0; i < drawCallsPerState; i++) {

                gl4.glDrawElements(GL4.GL_TRIANGLES, indexCount, GL4.GL_UNSIGNED_SHORT, 0);
            }
        } else {
            /**
             * Render using Vertex Array Objects (VAO).
             */
            // Set up attribute 0 for the position (3 floats)
            gl4.glVertexArrayVertexBuffer(0, 0, vertexBuffer[0], Vertex.positionOffset,
                    Vertex.size());
            gl4.glVertexArrayAttribFormat(0, 0, 3, GL4.GL_FLOAT, false, Vertex.size());

            // Set up attribute 1 for the color (4 unsigned bytes)
            gl4.glVertexArrayVertexBuffer(0, 1, vertexBuffer[0], Vertex.colorOffset,
                    Vertex.size());
            gl4.glVertexArrayAttribFormat(0, 1, 4, GL4.GL_UNSIGNED_BYTE, true, Vertex.size());

            // Set up a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glVertexArrayVertexBuffer(0, 3, vertexBuffer[0], Vertex.attrib1Offset,
                        Vertex.size());
                gl4.glVertexArrayAttribFormat(0, 3, 4, GL4.GL_FLOAT, false, Vertex.size());

                gl4.glVertexArrayVertexBuffer(0, 4, vertexBuffer[0], Vertex.attrib2Offset,
                        Vertex.size());
                gl4.glVertexArrayAttribFormat(0, 4, 4, GL4.GL_FLOAT, false, Vertex.size());

                gl4.glVertexArrayVertexBuffer(0, 5, vertexBuffer[0], Vertex.attrib3Offset,
                        Vertex.size());
                gl4.glVertexArrayAttribFormat(0, 5, 4, GL4.GL_FLOAT, false, Vertex.size());

                gl4.glVertexArrayVertexBuffer(0, 6, vertexBuffer[0], Vertex.attrib4Offset,
                        Vertex.size());
                gl4.glVertexArrayAttribFormat(0, 6, 4, GL4.GL_FLOAT, false, Vertex.size());

                gl4.glVertexArrayVertexBuffer(0, 7, vertexBuffer[0], Vertex.attrib5Offset,
                        Vertex.size());
                gl4.glVertexArrayAttribFormat(0, 7, 4, GL4.GL_FLOAT, false, Vertex.size());
            }

            // Set up the indices
            gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);

            // Do the actual drawing
            for (int i = 0; i < drawCallsPerState; i++) {

                gl4.glDrawElements(GL4.GL_TRIANGLES, indexCount, GL4.GL_UNSIGNED_SHORT, 0);
            }
        }
    }

    /**
     * Resets state related to the vertex format.
     *
     * @param gl4
     */
    public static void renderFinish(GL4 gl4) {

        if (enableVBUM) {

            // Reset state
            gl4.glDisableVertexAttribArray(0);
            gl4.glDisableVertexAttribArray(1);
            gl4.glDisableVertexAttribArray(2);

            // Disable a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glDisableVertexAttribArray(3);
                gl4.glDisableVertexAttribArray(4);
                gl4.glDisableVertexAttribArray(5);
                gl4.glDisableVertexAttribArray(6);
                gl4.glDisableVertexAttribArray(7);
            }

            gl4.glDisableClientState(GL4.GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);
            gl4.glDisableClientState(GL4.GL_ELEMENT_ARRAY_UNIFIED_NV);

        } else {

            // Rendering with Vertex Array Objects (VAO)
            // Reset state
            gl4.glDisableVertexArrayAttrib(0, 0);
            gl4.glDisableVertexArrayAttrib(0, 1);

            // Disable a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glDisableVertexArrayAttrib(0, 3);
                gl4.glDisableVertexArrayAttrib(0, 4);
                gl4.glDisableVertexArrayAttrib(0, 5);
                gl4.glDisableVertexArrayAttrib(0, 6);
                gl4.glDisableVertexArrayAttrib(0, 7);
            }

            gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

}
