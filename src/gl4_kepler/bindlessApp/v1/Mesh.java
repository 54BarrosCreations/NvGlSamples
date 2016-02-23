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
package gl4_kepler.bindlessApp.v1;

import com.jogamp.opengl.GL;
import gl4_kepler.bindlessApp.*;
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
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import nvAppBase.Semantic;

/**
 *
 * @author GBarbieri
 */
public class Mesh {

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);    // vertex buffer object
//    private IntBuffer bufferSize = GLBuffers.newDirectIntBuffer(Buffer.MAX);    // buffer size
//    private LongBuffer gpuPointerBuffer = GLBuffers.newDirectLongBuffer(Buffer.MAX);   // GPU pointer

//    public static boolean enableVBUM = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;
    public static final boolean useDSA = false;
    public static final boolean useVAB = false;
    public static final boolean useGlobalVAO = false;
    public IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);

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
     * @param elements
     */
    public void init(GL4 gl4, Vertex[] vertices, short[] elements) {

        if (useDSA) {
            gl4.glCreateBuffers(Buffer.MAX, bufferName);
            if (!useGlobalVAO) {
                gl4.glCreateVertexArrays(1, vertexArrayName);
            }
        } else {
            gl4.glGenBuffers(Buffer.MAX, bufferName);
            if (!useGlobalVAO) {
                gl4.glGenVertexArrays(1, vertexArrayName);
            }
        }

        // Stick the data for the vertices and indices in their respective buffers
//        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(Vertex.SIZE * vertices.length);
        for (Vertex vertice : vertices) {
            vertice.toBb(vertexBuffer);
        }
        vertexBuffer.rewind();
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elements);

        if (useDSA) {
            gl4.glNamedBufferData(bufferName.get(Buffer.VERTEX), vertexBuffer.capacity() * Byte.BYTES, vertexBuffer,
                    GL_STATIC_DRAW);
            gl4.glNamedBufferData(bufferName.get(Buffer.ELEMENT), elements.length * Short.BYTES, elementBuffer,
                    GL_STATIC_DRAW);
        } else {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Byte.BYTES, vertexBuffer, GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Short.BYTES, elementBuffer,
                    GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        if (useVAB) {

            if (useDSA) {

                // Set up attribute 0 for the position (3 floats)
                gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 3, GL_FLOAT, false,
                        Vertex.PositionOffset);
                // Set up attribute 1 for the color (4 unsigned bytes)
                gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true,
                        Vertex.ColorOffset);

                int bindingIndex = 0;
                gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, bindingIndex);
                gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.COLOR, bindingIndex);

                // Set up a bunch of other attributes if we're using the heavy vertex format option
                if (useHeavyVertexFormat) {

                    gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.ATTR0, 4, GL_FLOAT, false,
                            Vertex.Attrib0Offset);
                    gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.ATTR1, 4, GL_FLOAT, false,
                            Vertex.Attrib1Offset);
                    gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.ATTR2, 4, GL_FLOAT, false,
                            Vertex.Attrib2Offset);
                    gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.ATTR3, 4, GL_FLOAT, false,
                            Vertex.Attrib3Offset);
                    gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.ATTR4, 4, GL_FLOAT, false,
                            Vertex.Attrib4Offset);

                    gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.ATTR0, bindingIndex);
                    gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.ATTR1, bindingIndex);
                    gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.ATTR2, bindingIndex);
                    gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.ATTR3, bindingIndex);
                    gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.ATTR4, bindingIndex);
                }

                // For Vertex Array Objects (VAO), enable the vertex attributes
                gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);
                gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.COLOR);

                // Enable a bunch of other attributes if we're using the heavy vertex format option
                if (useHeavyVertexFormat) {

                    gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.ATTR0);
                    gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.ATTR1);
                    gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.ATTR2);
                    gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.ATTR3);
                    gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.ATTR4);
                }
                // bind element array
                gl4.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT));

            } else {

                gl4.glBindVertexArray(vertexArrayName.get(0));
                {
                    // Set up attribute 0 for the position (3 floats)
                    gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex.PositionOffset);
                    // Set up attribute 1 for the color (4 unsigned bytes)
                    gl4.glVertexAttribFormat(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex.ColorOffset);

                    int bindingIndex = 0;
                    gl4.glVertexAttribBinding(Semantic.Attr.POSITION, bindingIndex);
                    gl4.glVertexAttribBinding(Semantic.Attr.COLOR, bindingIndex);

                    // Set up a bunch of other attributes if we're using the heavy vertex format option
                    if (useHeavyVertexFormat) {

                        gl4.glVertexAttribFormat(Semantic.Attr.ATTR0, 4, GL_FLOAT, false, Vertex.Attrib0Offset);
                        gl4.glVertexAttribFormat(Semantic.Attr.ATTR1, 4, GL_FLOAT, false, Vertex.Attrib1Offset);
                        gl4.glVertexAttribFormat(Semantic.Attr.ATTR2, 4, GL_FLOAT, false, Vertex.Attrib2Offset);
                        gl4.glVertexAttribFormat(Semantic.Attr.ATTR3, 4, GL_FLOAT, false, Vertex.Attrib3Offset);
                        gl4.glVertexAttribFormat(Semantic.Attr.ATTR4, 4, GL_FLOAT, false, Vertex.Attrib4Offset);

                        gl4.glVertexAttribBinding(Semantic.Attr.ATTR0, bindingIndex);
                        gl4.glVertexAttribBinding(Semantic.Attr.ATTR1, bindingIndex);
                        gl4.glVertexAttribBinding(Semantic.Attr.ATTR2, bindingIndex);
                        gl4.glVertexAttribBinding(Semantic.Attr.ATTR3, bindingIndex);
                        gl4.glVertexAttribBinding(Semantic.Attr.ATTR4, bindingIndex);
                    }

                    // For Vertex Array Objects (VAO), enable the vertex attributes
                    gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
                    gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

                    // Enable a bunch of other attributes if we're using the heavy vertex format option
                    if (useHeavyVertexFormat) {

                        gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR0);
                        gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR1);
                        gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR2);
                        gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR3);
                        gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR4);
                    }

                    gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
                }
                gl4.glBindVertexArray(0);
            }
        } else {

            gl4.glBindVertexArray(vertexArrayName.get(0));
            {
                gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
                {
                    gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex.SIZE,
                            Vertex.PositionOffset);
                    gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex.SIZE,
                            Vertex.ColorOffset);

                    if (useHeavyVertexFormat) {

                        gl4.glVertexAttribPointer(Semantic.Attr.ATTR0, 4, GL_FLOAT, false, Vertex.SIZE,
                                Vertex.Attrib0Offset);
                        gl4.glVertexAttribPointer(Semantic.Attr.ATTR1, 4, GL_FLOAT, false, Vertex.SIZE,
                                Vertex.Attrib1Offset);
                        gl4.glVertexAttribPointer(Semantic.Attr.ATTR2, 4, GL_FLOAT, false, Vertex.SIZE,
                                Vertex.Attrib2Offset);
                        gl4.glVertexAttribPointer(Semantic.Attr.ATTR3, 4, GL_FLOAT, false, Vertex.SIZE,
                                Vertex.Attrib3Offset);
                        gl4.glVertexAttribPointer(Semantic.Attr.ATTR4, 4, GL_FLOAT, false, Vertex.SIZE,
                                Vertex.Attrib4Offset);
                    }
                }
                gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

                gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
                gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

                if (useHeavyVertexFormat) {

                    gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR0);
                    gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR1);
                    gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR2);
                    gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR3);
                    gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR4);
                }

                gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            }
            gl4.glBindVertexArray(0);
        }
    }

    /**
     * Does the actual rendering of the mesh.
     *
     * @param gl4
     */
    public void render(GL4 gl4) {

        ////////////////////////////////////////////////////////////////////////////////
        //
        //             Render using Vertex Array Objects (VAO)
        //
        ////////////////////////////////////////////////////////////////////////////////
        if (useVAB) {

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
            gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.POSITION);
            gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.COLOR);

            // Disable a bunch of other attributes if we're using the heavy vertex format option
            if (useHeavyVertexFormat) {

                gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.ATTR3);
                gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.ATTR4);
                gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.ATTR5);
                gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.ATTR6);
                gl4.glDisableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.ATTR7);
            }

            gl4.glVertexArrayElementBuffer(vertexArrayName[0], 0);
        } else {
            gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
}
