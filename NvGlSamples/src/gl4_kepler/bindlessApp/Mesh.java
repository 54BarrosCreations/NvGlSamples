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
import static com.jogamp.opengl.GL2ES3.GL_READ_ONLY;
import static com.jogamp.opengl.GL2GL3.GL_BUFFER_GPU_ADDRESS_NV;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Mesh {

    private int vertexCount;            // Number of vertices in mesh
    private int indexCount;             // Number of indices in mesh
    private int[] vertexBuffer = {0};           // vertex buffer object for vertices
    private int[] indexBuffer = {0};            // vertex buffer object for indices
    private int paramsBuffer;           // uniform buffer object for params
    private int[] vertexBufferSize = {0};
    private int[] indexBufferSize = {0};
    private long[] vertexBufferGPUPtr = {0};     // GPU pointer to m_vertexBuffer data
    private long[] indexBufferGPUPtr = {0};      // GPU pointer to m_indexBuffer data

    public static boolean enableVBUM;
    public static boolean setVertexFormatOnEveryDrawCall;
    public static boolean useHeavyVertexFormat;
    public static int drawCallsPerState;

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
            gl4.glGenBuffers(1, vertexBuffer, 0);
        }

        if (indexCount == 0) {
            gl4.glGenBuffers(1, indexBuffer, 0);
        }

        // Stick the data for the vertices and indices in their respective buffers
        ByteBuffer verticesBuffer = GLBuffers.newDirectByteBuffer(Vertex.SIZEOF * vertices.length);
        for (Vertex vertex : vertices) {
            verticesBuffer.put(vertex.toByteArray());
        }
        gl4.glNamedBufferData(vertexBuffer[0], Vertex.SIZEOF * vertices.length, verticesBuffer.rewind(), vertexCount);

        ShortBuffer indicesBuffer = GLBuffers.newDirectShortBuffer(indices);
        gl4.glNamedBufferData(indexBuffer[0], Short.BYTES * indices.length, indicesBuffer, vertexCount);

        // *** INTERESTING ***
        // get the GPU pointer for the vertex buffer and make the vertex buffer resident on the GPU
        gl4.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0]);
        gl4.glGetBufferParameterui64vNV(GL_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, vertexBufferGPUPtr, 0);
        gl4.glGetBufferParameteriv(GL_ARRAY_BUFFER, GL_BUFFER_SIZE, vertexBufferSize, 0);
        gl4.glMakeBufferResidentNV(GL_ARRAY_BUFFER, GL_READ_ONLY);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        // *** INTERESTING ***
        // get the GPU pointer for the index buffer and make the index buffer resident on the GPU
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);
        gl4.glGetBufferParameterui64vNV(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV,  indexBufferGPUPtr,0);
        gl4.glGetBufferParameteriv(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_SIZE,  indexBufferSize,0);
        gl4.glMakeBufferResidentNV(GL_ELEMENT_ARRAY_BUFFER, GL_READ_ONLY);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
