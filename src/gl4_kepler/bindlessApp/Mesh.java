//----------------------------------------------------------------------------------
// File:        gl4-kepler/BindlessApp/Mesh.cpp
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

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BUFFER_SIZE;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES3.GL_READ_ONLY;
import static com.jogamp.opengl.GL2GL3.GL_BUFFER_GPU_ADDRESS_NV;
import static com.jogamp.opengl.GL2GL3.GL_ELEMENT_ARRAY_ADDRESS_NV;
import static com.jogamp.opengl.GL2GL3.GL_ELEMENT_ARRAY_UNIFIED_NV;
import static com.jogamp.opengl.GL2GL3.GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV;
import static com.jogamp.opengl.GL2GL3.GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import nvAppBase.Semantic;

/**
 *
 * @author GBarbieri
 */
public class Mesh {

    private int vertexCount;            // Number of vertices in mesh
    private int indexCount;             // Number of indices in mesh
    private int[] vertexBuffer = {0};           // vertex buffer object for vertices
    private int[] indexBuffer = {0};            // vertex buffer object for indices
    private int[] vertexBufferSize = {0};
    private int[] indexBufferSize = {0};
    private long[] vertexBufferGPUPtr = {0};     // GPU pointer to m_vertexBuffer data
    private long[] indexBufferGPUPtr = {0};      // GPU pointer to m_indexBuffer data

    public static boolean enableVBUM = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;    
    public static final boolean useDSA = false;
    public static int[] vao = {0};

    public Mesh() {

    }

    /**
     * This method is called to update the vertex and index data for the mesh.
     * For GL_NV_vertex_buffer_unified_memory (VBUM), we ask the driver to give us
     * GPU pointers for the buffers. Later, when we render, we use these GPU pointers
     * directly. By using GPU pointers, the driver can avoid many system memory
     * accesses which pollute the CPU caches and reduce performance.
     *
     * @param gl4
     * @param vertices
     * @param indices
     */
    public void update(GL4 gl4, Vertex[] vertices, short[] indices) {

        if (vertexCount == 0) {
            gl4.glCreateBuffers(1, vertexBuffer, 0);
        }

        if (indexCount == 0) {
            gl4.glCreateBuffers(1, indexBuffer, 0);
        }

        // Stick the data for the vertices and indices in their respective buffers
        ByteBuffer verticesBuffer = ByteBuffer.allocateDirect(Vertex.SIZE * vertices.length);
        for (Vertex vertex : vertices) {
            verticesBuffer.put(vertex.toByteBuffer());
        }
        verticesBuffer.rewind();
        if (useDSA) {
            gl4.glNamedBufferData(vertexBuffer[0], verticesBuffer.capacity() * Byte.BYTES, verticesBuffer, GL_STATIC_DRAW);
        } else {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0]);
            gl4.glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * Byte.BYTES, verticesBuffer, GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        Buffer indicesBuffer = GLBuffers.newDirectShortBuffer(indices).rewind();
        if (useDSA) {
            gl4.glNamedBufferData(indexBuffer[0], indices.length * Short.BYTES, indicesBuffer, GL_STATIC_DRAW);
        } else {
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);
            gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * Short.BYTES, indicesBuffer, GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        // *** INTERESTING ***
        // get the GPU pointer for the vertex buffer and make the vertex buffer resident on the GPU
//        gl4.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0]);
//        gl4.glGetBufferParameterui64vNV(GL_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, vertexBufferGPUPtr, 0);
//        gl4.glGetBufferParameteriv(GL_ARRAY_BUFFER, GL_BUFFER_SIZE, vertexBufferSize, 0);
//        gl4.glMakeBufferResidentNV(GL_ARRAY_BUFFER, GL_READ_ONLY);
//        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//        // *** INTERESTING ***
//        // get the GPU pointer for the index buffer and make the index buffer resident on the GPU
//        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);
//        gl4.glGetBufferParameterui64vNV(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, indexBufferGPUPtr, 0);
//        gl4.glGetBufferParameteriv(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_SIZE, indexBufferSize, 0);
//        gl4.glMakeBufferResidentNV(GL_ELEMENT_ARRAY_BUFFER, GL_READ_ONLY);
//        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//
        vertexCount = vertices.length;
        indexCount = indices.length;
    }

    /**
     * Sets up the vertex format state.
     *
     * @param gl4
     */
    public static void renderPrep(GL4 gl4) {

        if (enableVBUM) {

            // Specify the vertex format
            // Position in attribute 0 that is 3 floats
            gl4.glVertexAttribFormatNV(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex.SIZE);
            // Color in attribute 1 that is 4 unsigned bytes
            gl4.glVertexAttribFormatNV(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex.SIZE);

            // Enable the relevent attributes
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            // Enable a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {
                gl4.glVertexAttribFormatNV(Semantic.Attr.ATTR3, 4, GL_FLOAT, false, Vertex.SIZE);
                gl4.glVertexAttribFormatNV(Semantic.Attr.ATTR4, 4, GL_FLOAT, false, Vertex.SIZE);
                gl4.glVertexAttribFormatNV(Semantic.Attr.ATTR5, 4, GL_FLOAT, false, Vertex.SIZE);
                gl4.glVertexAttribFormatNV(Semantic.Attr.ATTR6, 4, GL_FLOAT, false, Vertex.SIZE);
                gl4.glVertexAttribFormatNV(Semantic.Attr.ATTR7, 4, GL_FLOAT, false, Vertex.SIZE);

                gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR3);
                gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR4);
                gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR5);
                gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR6);
                gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR7);
            }

            // Enable Vertex Buffer Unified Memory (VBUM) for the vertex attributes
            gl4.glEnableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);

            // Enable Vertex Buffer Unified Memory (VBUM) for the indices
            gl4.glEnableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV);

        } else if (useDSA) {
            // Set up attribute 0 for the position (3 floats)
            gl4.glVertexArrayAttribFormat(vao[0], Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex.PositionOffset);

            // Set up attribute 1 for the color (4 unsigned bytes)
            gl4.glVertexArrayAttribFormat(vao[0], Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex.ColorOffset);

            int bindingIndex = 0;
//            gl4.glVertexArrayVertexBuffer(globalVAO[0], bindingIndex, vertexBuffer[0], 0, Vertex.SIZEOF);
            gl4.glVertexArrayAttribBinding(vao[0], Semantic.Attr.POSITION, bindingIndex);
            gl4.glVertexArrayAttribBinding(vao[0], Semantic.Attr.COLOR, bindingIndex);

            // Set up a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glVertexArrayAttribFormat(vao[0], 3, 4, GL_FLOAT, false, Vertex.Attrib1Offset);
                gl4.glVertexArrayAttribFormat(vao[0], 4, 4, GL_FLOAT, false, Vertex.Attrib2Offset);
                gl4.glVertexArrayAttribFormat(vao[0], 5, 4, GL_FLOAT, false, Vertex.Attrib3Offset);
                gl4.glVertexArrayAttribFormat(vao[0], 6, 4, GL_FLOAT, false, Vertex.Attrib4Offset);
                gl4.glVertexArrayAttribFormat(vao[0], 7, 4, GL_FLOAT, false, Vertex.Attrib5Offset);

                gl4.glVertexArrayAttribBinding(vao[0], 3, bindingIndex);
                gl4.glVertexArrayAttribBinding(vao[0], 4, bindingIndex);
                gl4.glVertexArrayAttribBinding(vao[0], 5, bindingIndex);
                gl4.glVertexArrayAttribBinding(vao[0], 6, bindingIndex);
                gl4.glVertexArrayAttribBinding(vao[0], 7, bindingIndex);
            }
//            gl4.glVertexArrayElementBuffer(globalVAO[0], indexBuffer[0]);

            // For Vertex Array Objects (VAO), enable the vertex attributes
            gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.POSITION);
            gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.COLOR);

            // Enable a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR3);
                gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR4);
                gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR5);
                gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR6);
                gl4.glEnableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR7);
            }
        } else {
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
    }

    /**
     * Does the actual rendering of the mesh.
     *
     * @param gl4
     */
    public void render(GL4 gl4) {

        assert (vertexBuffer[0] != 0);
        assert (indexBuffer[0] != 0);

        if (enableVBUM) {
            ////////////////////////////////////////////////////////////////////////////////
            //
            //             Render using Vertex Buffer Unified Memory (VBUM)
            //
            ////////////////////////////////////////////////////////////////////////////////

            // *** INTERESTING ***
            // Set up the pointers in GPU memory to the vertex attributes.
            // The GPU pointer to the vertex buffer was stored in Mesh::update() after the buffer was filled
            gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.POSITION,
                    vertexBufferGPUPtr[0] + Vertex.PositionOffset,
                    vertexBufferSize[0] - Vertex.PositionOffset);
            gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.COLOR,
                    vertexBufferGPUPtr[0] + Vertex.ColorOffset,
                    vertexBufferSize[0] - Vertex.ColorOffset);

            // Set a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.ATTR3,
                        vertexBufferGPUPtr[0] + Vertex.Attrib1Offset, vertexBufferSize[0] - Vertex.Attrib1Offset);
                gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.ATTR4,
                        vertexBufferGPUPtr[0] + Vertex.Attrib2Offset, vertexBufferSize[0] - Vertex.Attrib2Offset);
                gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.ATTR5,
                        vertexBufferGPUPtr[0] + Vertex.Attrib3Offset, vertexBufferSize[0] - Vertex.Attrib3Offset);
                gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.ATTR6,
                        vertexBufferGPUPtr[0] + Vertex.Attrib4Offset, vertexBufferSize[0] - Vertex.Attrib4Offset);
                gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.ATTR7,
                        vertexBufferGPUPtr[0] + Vertex.Attrib5Offset, vertexBufferSize[0] - Vertex.Attrib5Offset);
            }

            // *** INTERESTING ***
            // Set up the pointer in GPU memory to the index buffer
            gl4.glBufferAddressRangeNV(GL_ELEMENT_ARRAY_ADDRESS_NV, 0, indexBufferGPUPtr[0], indexBufferSize[0]);

            // Do the actual drawing
            for (int i = 0; i < drawCallsPerState; i++) {

                gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
            }
        } else {
            ////////////////////////////////////////////////////////////////////////////////
            //
            //             Render using Vertex Array Objects (VAO)
            //
            ////////////////////////////////////////////////////////////////////////////////

            if (useDSA) {
                // TODO
            } else {
                gl4.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0]);
                {
                    gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false,
                            Vertex.SIZE, Vertex.PositionOffset);
                    gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true,
                            Vertex.SIZE, Vertex.ColorOffset);
                }
                gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
            }
            // Set up the indices
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);

            // Do the actual drawing
            for (int i = 0; i < drawCallsPerState; i++) {
                gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
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

            gl4.glDisableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);
            gl4.glDisableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV);

        } else if (useDSA) {
            // Rendering with Vertex Array Objects (VAO)
            // Reset state
            gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.POSITION);
            gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.COLOR);

            // Disable a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR3);
                gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR4);
                gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR5);
                gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR6);
                gl4.glDisableVertexArrayAttrib(vao[0], Semantic.Attr.ATTR7);
            }

            gl4.glVertexArrayElementBuffer(vao[0], 0);
        } else {
            gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
}
